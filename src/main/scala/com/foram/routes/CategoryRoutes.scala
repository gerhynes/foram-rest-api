package com.foram.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.pattern.ask
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.foram.Main.{categoryActor, topicActor}
import com.foram.actors.CategoryActor._
import com.foram.actors.TopicActor._
import com.foram.models.{Category, CategoryWithTopics, Topic}
import spray.json.DefaultJsonProtocol._

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object CategoryRoutes {

  import com.foram.JsonFormats._

  implicit val timeout = Timeout(5 seconds)

  val routes = cors() {
    handleExceptions(customExceptionHandler) {
      pathPrefix("api" / "categories") {
        get {
          path(Segment / "topics") { category_id =>
            val uuid = UUID.fromString(category_id)
            complete((topicActor ? GetTopicsByCategoryID(uuid)).mapTo[List[Topic]])
          } ~
            path(Segment) { id =>
              val uuid = UUID.fromString(id)
              complete((categoryActor ? GetCategoryByID(uuid)).mapTo[Category])
            } ~
            pathEndOrSingleSlash {
              complete((categoryActor ? GetAllCategories).mapTo[List[Category]])
            }
        } ~
          post {
            entity(as[CategoryWithTopics]) { categoryWithTopics =>
              complete((categoryActor ? CreateCategory(categoryWithTopics)).map(_ => StatusCodes.Created))
            }
          } ~
          put {
            path(Segment) { id =>
              entity(as[Category]) { category =>
                val uuid = UUID.fromString(id)
                complete((categoryActor ? UpdateCategory(uuid, category)).map(_ => StatusCodes.OK))
              }
            }
          } ~
          delete {
            path(Segment) { id =>
              val uuid = UUID.fromString(id)
              complete((categoryActor ? DeleteCategory(uuid)).map(_ => StatusCodes.OK))
            }
          }
      }
    }
  }

  implicit val customExceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: NoSuchElementException =>
      complete(StatusCodes.NotFound, "Cannot find resource")
    case e: ClassCastException =>
      complete(StatusCodes.NotFound, "Incorrect resource returned")
    case e: RuntimeException =>
      complete(StatusCodes.NotFound, e.getMessage)
    case e: IllegalArgumentException =>
      complete(StatusCodes.BadRequest, "Illegal argument passed")
  }
}