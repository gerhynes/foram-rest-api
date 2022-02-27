package com.foram.routes

import scala.concurrent._
import ExecutionContext.Implicits.global
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import akka.util.Timeout
import akka.pattern.ask
import com.foram.actors.{Category, Topic}
import com.foram.actors.CategoryDB._
import com.foram.Main.{categoryDB, topicDB}
import com.foram.actors.TopicDB._

import scala.concurrent.duration._

class CategoryRoutes {

  import com.foram.JsonFormats._

  implicit val timeout = Timeout(2 seconds)

  val categoryRoutes =
    pathPrefix("api" / "categories") {
      get {
          path(IntNumber / "topics") { category_id =>
            complete((topicDB ? GetTopicsByCategory(category_id)).mapTo[List[Topic]])
        } ~
          path(IntNumber) { id =>
            complete((categoryDB ? GetCategory(id)).mapTo[Option[Category]])
          } ~
          pathEndOrSingleSlash {
            complete((categoryDB ? GetAllCategories).mapTo[List[Category]])
          }
      } ~
        post {
          entity(as[Category]) { category =>
            complete((categoryDB ? AddCategory(category)).map(_ => StatusCodes.OK))
          }
        } ~
        put {
          path(IntNumber) { id =>
            entity(as[Category]) { category =>
              complete((categoryDB ? UpdateCategory(id, category)).map(_ => StatusCodes.OK))
            }
          }
        } ~
        delete {
          entity(as[Category]) { category =>
            complete((categoryDB ? RemoveCategory(category)).map(_ => StatusCodes.OK))
          }
        }
    }
}
