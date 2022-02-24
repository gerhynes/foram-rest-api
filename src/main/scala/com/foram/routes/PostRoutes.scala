package com.foram.routes

import scala.concurrent._
import ExecutionContext.Implicits.global
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import akka.util.Timeout
import akka.pattern.ask
import com.foram.Main.postDB
import com.foram.actors.Post
import com.foram.actors.PostDB._

import scala.concurrent.Future
import scala.concurrent.duration._

class PostRoutes {

  import com.foram.JsonFormats._

  implicit val timeout = Timeout(2 seconds)

  val postRoutes =
    pathPrefix("api" / "posts") {
      get {
        (path(IntNumber) | parameter('id.as[Int])) { id =>
          val postOptionFuture: Future[Option[Post]] = (postDB ? GetPost(id)).mapTo[Option[Post]]
          complete(postOptionFuture)
        } ~
          pathEndOrSingleSlash {
            complete((postDB ? GetAllPosts).mapTo[List[Post]])
          }
      } ~
        post {
          entity(as[Post]) { post =>
            complete((postDB ? AddPost(post)).map(_ => StatusCodes.OK))
          }
        } ~
        put {
          (path(IntNumber) | parameter('id.as[Int])) { id =>
            entity(as[Post]) { post =>
              complete((postDB ? UpdatePost(id, post)).map(_ => StatusCodes.OK))
            }
          }
        } ~
        delete {
          entity(as[Post]) { post =>
            complete((postDB ? RemovePost(post)).map(_ => StatusCodes.OK))
          }
        }
    }
}
