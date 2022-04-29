package com.foram.routes

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.foram.actors.PostActor._
import com.foram.auth.Auth.authenticated
import com.foram.models.{Message, Post}
import spray.json.DefaultJsonProtocol._

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class PostRoutes(postActor: ActorRef) {

  import com.foram.json.JsonFormats._

  implicit val timeout = Timeout(5 seconds)

  val routes =
    pathPrefix("api" / "posts") {
      get {
        path(Segment) { id =>
          val uuid = UUID.fromString(id)
          complete((postActor ? GetPostByID(uuid)).mapTo[Post])
        } ~
          pathEndOrSingleSlash {
            complete((postActor ? GetAllPosts).mapTo[List[Post]])
          }
      } ~
        authenticated { claims => {
          post {
            entity(as[Post]) { post =>
              onComplete((postActor ? CreatePost(post)).mapTo[Post]) {
                case Success(createdPost) => complete(StatusCodes.Created, createdPost)
                case Failure(ex) => complete(StatusCodes.InternalServerError)
              }
            }
          } ~
            put {
              path(Segment) { id =>
                val uuid = UUID.fromString(id)
                entity(as[Post]) { post =>
                  onComplete((postActor ? UpdatePost(uuid, post)).mapTo[Message])  {
                    case Success(message) => complete(StatusCodes.OK, message)
                    case Failure(ex) => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            } ~
            delete {
              path(Segment) { id =>
                val uuid = UUID.fromString(id)
                onComplete((postActor ? DeletePost(uuid)).mapTo[Message]) {
                  case Success(message) => complete(StatusCodes.OK, message)
                  case Failure(ex) => complete(StatusCodes.InternalServerError)
                }
              }
            }
        }
        }
    }

}