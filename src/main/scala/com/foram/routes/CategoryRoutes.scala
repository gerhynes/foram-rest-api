package com.foram.routes

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.foram.actors.CategoryActor._
import com.foram.actors.TopicActor._
import com.foram.auth.Auth.authenticated
import com.foram.models.{Category, CategoryWithChildren, Message, Topic}
import spray.json.DefaultJsonProtocol._

import java.util.UUID
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class CategoryRoutes(categoryActor: ActorRef, topicActor: ActorRef) {

  import com.foram.JsonFormats._

  implicit val timeout = Timeout(5 seconds)

  val routes =
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
        authenticated { claims => {
          post {
            entity(as[CategoryWithChildren]) { newCategory =>
              onComplete((categoryActor ? CreateCategory(newCategory)).mapTo[Category]) {
                case Success(createdCategory) => complete(StatusCodes.Created, createdCategory)
                case Failure(ex) => complete(StatusCodes.InternalServerError)
              }
            }
          } ~
            put {
              path(Segment) { id =>
                entity(as[Category]) { category =>
                  val uuid = UUID.fromString(id)
                  onComplete((categoryActor ? UpdateCategory(uuid, category)).mapTo[Message]) {
                    case Success(message) => complete(StatusCodes.OK, message)
                    case Failure(ex) => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            } ~
            delete {
              path(Segment) { id =>
                val uuid = UUID.fromString(id)
                onComplete((categoryActor ? DeleteCategory(uuid)).mapTo[Message]) {
                  case Success(message) => complete(StatusCodes.OK, message)
                  case Failure(ex) => complete(StatusCodes.InternalServerError)
                }
              }
            }
        }
        }
    }
}