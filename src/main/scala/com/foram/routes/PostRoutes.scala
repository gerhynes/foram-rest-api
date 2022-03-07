package com.foram.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.foram.Main.postActor
import com.foram.actors.PostActor._
import com.foram.models.Post
import spray.json.DefaultJsonProtocol._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object PostRoutes {

  import com.foram.JsonFormats._

  implicit val timeout = Timeout(5 seconds)

  val routes =
    pathPrefix("api" / "posts") {
      get {
        path(IntNumber) { id =>
          complete((postActor ? GetPostByID(id)).mapTo[Post])
        } ~
          pathEndOrSingleSlash {
            complete((postActor ? GetAllPosts).mapTo[List[Post]])
          }
      } ~
        post {
          entity(as[Post]) { post =>
            complete((postActor ? CreatePost(post)).map(_ => StatusCodes.OK))
          }
        } ~
        put {
          path(IntNumber) { id =>
            entity(as[Post]) { post =>
              complete((postActor ? UpdatePost(id, post)).map(_ => StatusCodes.OK))
            }
          }
        } ~
        delete {
          path(IntNumber) { id =>
            complete((postActor ? DeletePost(id)).map(_ => StatusCodes.OK))
          }
        }
    }
}