package com.foram.routes

import scala.concurrent._
import ExecutionContext.Implicits.global
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import akka.util.Timeout
import akka.pattern.ask
import com.foram.Main.{postDB, userDB}
import com.foram.actors.PostDB.{GetPostsByUsername}
import com.foram.actors.{Post, User}
import com.foram.actors.UserDB._

import scala.concurrent.duration._

class UserRoutes {

  import com.foram.JsonFormats._

  implicit val timeout = Timeout(2 seconds)

  val userRoutes =
    pathPrefix("api" / "users") {
      get {
        path(Segment / "posts") { username =>
          complete((postDB ? GetPostsByUsername(username)).mapTo[List[Post]])
        } ~
        path(Segment) { username =>
          complete((userDB ? GetUserByUsername(username)).mapTo[Option[User]])
        } ~
          pathEndOrSingleSlash {
            complete((userDB ? GetAllUsers).mapTo[List[User]])
          }
      } ~
        post {
          entity(as[User]) { user =>
            complete((userDB ? AddUser(user)).map(_ => StatusCodes.OK))
          }
        } ~
        put {
          path(IntNumber) { id =>
            entity(as[User]) { user =>
              complete((userDB ? UpdateUser(id, user)).map(_ => StatusCodes.OK))
            }
          }
        } ~
        delete {
          entity(as[User]) { user =>
            complete((userDB ? RemoveUser(user)).map(_ => StatusCodes.OK))
          }
        }
    }
}
