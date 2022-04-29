package com.foram.routes

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.foram.actors.UserActor.GetUserByUsername
import com.foram.auth.Auth.{createToken, validatePassword}
import com.foram.models.{LoginRequest, Message, RegisteredUser, User}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class AuthRoutes(userActor: ActorRef) {

  import com.foram.json.JsonFormats._

  implicit val timeout: Timeout = Timeout(5 seconds)

  val routes: Route = {
    (pathPrefix("api" / "login") & post) {
      entity(as[LoginRequest]) {
        case LoginRequest(username, password) =>
          onComplete((userActor ? GetUserByUsername(username)).mapTo[User]) {
            case Success(user) =>
              if (validatePassword(password, user.password)) {
                val token = createToken(username, 7)
                // Add token to user
                val registeredUser = user match {
                  case User(id, name, username, email, password, role, created_at, updated_at) =>
                    RegisteredUser(id, name, username, email, role, created_at, updated_at, token)
                }
                complete(StatusCodes.OK, registeredUser)
              } else {
                complete(StatusCodes.Unauthorized, Message("Invalid username or password"))
              }
            case Failure(ex) => complete(StatusCodes.Unauthorized, Message("Invalid username or password"))
          }
        case _ => complete(StatusCodes.BadRequest, Message("Please submit valid username and password"))
      }
    }
  }
}
