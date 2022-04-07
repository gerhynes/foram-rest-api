package com.foram.auth

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, provide}
import com.github.t3hnar.bcrypt._
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}

import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success}

object Auth {
  val algorithm = JwtAlgorithm.HS256
  val secretKey = "thisshouldbeabettersecret"

  def hashPassword(value: String): String = {
    value.bcryptSafeBounded match {
      case Success(result) => result
      case Failure(ex) => throw ex
    }
  }

  def validatePassword(password: String, hash: String): Boolean = {
    password.isBcryptedSafeBounded(hash) match {
      case Success(result) => result
      case Failure(ex) => throw ex
    }
  }

  def createToken(username: String, expirationPeriodInDays: Int): String = {
    val claims = JwtClaim(
      expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.DAYS.toSeconds(expirationPeriodInDays)),
      issuedAt = Some(System.currentTimeMillis() / 1000),
      issuer = Some("foram.com")
    )

    // JWT String
    JwtSprayJson.encode(claims, secretKey, algorithm)
  }

  def isTokenExpired(token: String): Boolean = JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
    case Success(claims) => claims.expiration.getOrElse(0L) < System.currentTimeMillis() / 1000
    case Failure(ex) => throw ex
  }

  def isTokenValid(token: String): Boolean = JwtSprayJson.isValid(token, secretKey, Seq(algorithm))

  def authenticated: Directive1[String] = {
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(token) =>
        if (isTokenValid(token)) {
          if (isTokenExpired(token)) {
            complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token expired"))
          } else {
            JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
              case Success(claims) => provide(claims.content)
              case Failure(failure) =>
                complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token is invalid, or has been tampered with"))
            }
          }
        } else {
          complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token is invalid, or has been tampered with"))
        }
      case _ => complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "No token was provided"))
    }
  }

}
