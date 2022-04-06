package com.foram.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.pattern.ask
import akka.util.Timeout
import com.foram.Main.{postActor, topicActor, userActor}
import com.foram.actors.PostActor._
import com.foram.actors.TopicActor._
import com.foram.actors.UserActor._
import com.foram.models.{Post, Topic, User}
import com.foram.auth.Auth.authenticated
import spray.json.DefaultJsonProtocol._

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object UserRoutes {

  import com.foram.JsonFormats._

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
        authenticated { claims => {
          post {
            entity(as[User]) { user =>
              complete((userActor ? CreateUser(user)).map(_ => StatusCodes.Created))
            }
          } ~
            put {
              path(Segment) { id =>
                val uuid = UUID.fromString(id)
                entity(as[User]) { user =>
                  complete((userActor ? UpdateUser(uuid, user)).map(_ => StatusCodes.OK))
                }
              }
            } ~
            delete {
              path(Segment) { id =>
                complete((userActor ? DeleteUser(UUID.fromString(id))).map(_ => StatusCodes.OK))
              }
            }
        }
        }
    }
}