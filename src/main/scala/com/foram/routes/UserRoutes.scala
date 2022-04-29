package com.foram.routes

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.foram.actors.PostActor._
import com.foram.actors.TopicActor._
import com.foram.actors.UserActor._
import com.foram.auth.Auth
import com.foram.auth.Auth.authenticated
import com.foram.models.{Message, Post, RegisteredUser, Topic, User}
import spray.json.DefaultJsonProtocol._

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class UserRoutes(userActor: ActorRef, topicActor: ActorRef, postActor: ActorRef) {

  import com.foram.json.JsonFormats._

  implicit val timeout = Timeout(5 seconds)

  val routes =
    pathPrefix("api" / "users") {
      get {
        path(Segment / "topics") { username =>
          complete((topicActor ? GetTopicsByUsername(username)).mapTo[List[Topic]])
        } ~
          path(Segment / "posts") { username =>
            complete((postActor ? GetPostsByUsername(username)).mapTo[List[Post]])
          } ~
          path(Segment) { username =>
            complete((userActor ? GetUserByUsername(username)).mapTo[User])
          } ~
          pathEndOrSingleSlash {
            complete((userActor ? GetAllUsers).mapTo[List[User]])
          }
      } ~
        post {
          entity(as[User]) { user =>
            onComplete((userActor ? CreateUser(user)).mapTo[RegisteredUser]) {
              case Success(registeredUser) => complete(StatusCodes.Created, registeredUser)
              case Failure(ex) => complete(StatusCodes.InternalServerError, Message(ex.getMessage))
            }
          }
        } ~
        authenticated { claims => {
          put {
            path(Segment) { id =>
              val uuid = UUID.fromString(id)
              entity(as[User]) { user =>
                onComplete((userActor ? UpdateUser(uuid, user)).mapTo[Message]) {
                  case Success(message) => complete(StatusCodes.OK, message)
                  case Failure(ex) => complete(StatusCodes.InternalServerError, Message(ex.getMessage))
                }
              }
            }
          } ~
            delete {
              path(Segment) { id =>
                onComplete((userActor ? DeleteUser(UUID.fromString(id))).mapTo[Message]) {
                  case Success(message) => complete(StatusCodes.OK, message)
                  case Failure(ex) => complete(StatusCodes.InternalServerError, Message(ex.getMessage))
                }
              }
            }
        }
        }
    }
}