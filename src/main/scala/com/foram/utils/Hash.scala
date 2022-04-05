package com.foram.utils

import com.github.t3hnar.bcrypt._

import scala.util.{Failure, Success, Try}

object Hash {
  def hashPassword(value: String): String = {
    value.bcryptSafeBounded match {
      case Success(result) => result
      case Failure(ex) => throw ex
    }
  }

  def validatePassword(password: String, hash: String): Boolean = {
    password.isBcryptedSafeBounded(hash) match {
      case Success(result) => result
      case Failure(failure) => false
    }
  }
}
