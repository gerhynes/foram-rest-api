package com.foram.utils

import com.github.t3hnar.bcrypt._
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}

import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success, Try}

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
      case Success(result) => true
      case Failure(failure) => false
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
    case Failure(failure) => true
  }

  def isTokenValid(token: String): Boolean = JwtSprayJson.isValid(token, secretKey, Seq(algorithm))

}
