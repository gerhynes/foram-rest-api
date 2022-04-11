package com.foram.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.pattern.ask
import akka.util.Timeout
import com.foram.Main.postActor
import com.foram.actors.PostActor._
import com.foram.auth.Auth.{authenticated, getUserDataFromClaims}
import com.foram.models.Post
import spray.json.DefaultJsonProtocol._

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object PostRoutes {

  import com.foram.JsonFormats._

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
                  complete((postActor ? UpdatePost(uuid, post)).map(_ => StatusCodes.OK))
                }
              }
            } ~
            delete {
              path(Segment) { id =>
                val uuid = UUID.fromString(id)
                complete((postActor ? DeletePost(uuid)).map(_ => StatusCodes.OK))
              }
            }
        }
        }
    }

}