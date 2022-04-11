package com.foram.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.foram.Main.userActor
import com.foram.actors.UserActor.GetUserByUsername
import com.foram.auth.Auth.{createToken, validatePassword}
import com.foram.models.{LoginRequest, RegisteredUser, User}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object AuthRoutes {

  import com.foram.JsonFormats._

  implicit val timeout = Timeout(5 seconds)

  val routes = {
    (pathPrefix("api" / "login") & post) {
      entity(as[LoginRequest]) {
        case LoginRequest(username, password) =>
          onComplete((userActor ? GetUserByUsername(username)).mapTo[User]) {
            case Success(user) =>
              if (validatePassword(password, user.password)) {
                val token = createToken(username, 7)
                // Add token to user
                val registeredUser = user match {
                  case User(id, name, username, email, password, role, created_at, updated_at) => RegisteredUser(id, name, username, email, role, created_at, updated_at, token)
                }
                complete(StatusCodes.OK, registeredUser)
              } else {
                complete(HttpResponse(status = StatusCodes.Unauthorized, entity = "Incorrect username or password"))
              }
            case Failure(failure) => complete(StatusCodes.Unauthorized)
          }
        case _ => complete(StatusCodes.Unauthorized)
      }
    }
  }

}
