package com.foram.routes

import java.util.concurrent.TimeUnit
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directive1
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}
import spray.json._

import scala.util.{Failure, Success}

object SecurityDomain extends DefaultJsonProtocol {
  case class LoginRequest(username: String, password: String)

  implicit val loginRequestFormat = jsonFormat2(LoginRequest)
}

object AuthRoutes extends App with SprayJsonSupport {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  import system.dispatcher
  import SecurityDomain._

  val mockUsersDB = Map(
    "admin" -> "admin",
    "quince" -> "password123"
  )

  val algorithm = JwtAlgorithm.HS256
  val secretKey = "thisshouldbeabettersecret"

  def checkPassword(username: String, password: String): Boolean = {
    mockUsersDB.contains(username) && mockUsersDB(username) == password
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

  val loginRoute = {
    (pathPrefix("api" / "login") & post) {
      entity(as[LoginRequest]) {
        case LoginRequest(username, password) if checkPassword(username, password) =>
          val token = createToken(username, 1)
          respondWithHeader(RawHeader("Access-Token", token)) {
            complete(StatusCodes.OK)
          }
        case _ => complete(StatusCodes.Unauthorized)
      }
    }
  }

  val authenticatedRoute =
    (pathPrefix("api" / "private") & get) {
      optionalHeaderValueByName("Authorization") {
        case Some(token) =>
          if (isTokenValid(token)) {
            if (isTokenExpired(token)) {
              complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token expired"))
            } else {
              complete("User accessed authenticated route")
            }
          } else {
            complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Token is invalid, or has been tampered with"))
          }
        case _ => complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "No token was provided"))
      }
    }

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

  val protectedRoute = {
    authenticated { claims => {
      (pathPrefix("api" / "private") & get) {
        complete("User accessed authenticated route")
      }
    }
    }
  }

  val routes = loginRoute ~ protectedRoute

  Http().bindAndHandle(routes, "localhost", 8080)
}
