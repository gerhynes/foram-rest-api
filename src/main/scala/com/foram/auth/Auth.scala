package com.foram.auth

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import com.github.t3hnar.bcrypt._
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

import java.time.Clock
import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success}

object Auth {

  val algorithm = JwtAlgorithm.HS256
  val secretKey = "thisshouldbeabettersecret"

  implicit val clock: Clock = Clock.systemUTC

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
    val usernameClaim = s"""{"username":"${username}"}"""
    Jwt.encode(JwtClaim({usernameClaim}).issuedNow.expiresIn(TimeUnit.DAYS.toSeconds(expirationPeriodInDays)), secretKey, algorithm)
  }

  def isTokenExpired(token: String): Boolean = Jwt.decode(token, secretKey, Seq(algorithm)) match {
    case Success(claims) => claims.expiration.getOrElse(0L) < System.currentTimeMillis() / 1000
    case Failure(ex) => throw ex
  }

  def isTokenValid(token: String): Boolean = Jwt.isValid(token, secretKey, Seq(algorithm))

  def authenticated: Directive1[String] = {
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(bearerToken) =>
        // Remove "Bearer " from token
        val token = bearerToken.split(" ")(1)
        if (isTokenValid(token)) {
          if (isTokenExpired(token)) {
            complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token expired"))
          } else {
            Jwt.decodeRaw(token, secretKey, Seq(algorithm)) match {
              case Success(claims) => provide(claims)
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