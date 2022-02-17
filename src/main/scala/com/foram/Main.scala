package com.foram

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Future
import scala.concurrent.duration._
import spray.json._

case class Category(id: Int, name: String, description: String)

object CategoryDB {
  case object GetAllCategories
  case class GetCategory(id: Int)
  case class AddCategory(category: Category)
  case class UpdateCategory(id: Int, category: Category)
  case class RemoveCategory(category: Category)
  case object OperationSuccess
}

class CategoryDB extends Actor with ActorLogging {
  import CategoryDB._

  var categories: Map[Int, Category] = Map()

  override def receive: Receive = {
    case GetAllCategories =>
      log.info(s"Searching for categories")
      sender() ! categories.values.toList

    case GetCategory(id) =>
      log.info(s"Finding category with name: $id")
      sender() ! categories.get(id)

    case AddCategory(category) =>
      log.info(s"Adding category $category")
      categories = categories + (category.id -> category)
      sender() ! OperationSuccess

    case UpdateCategory(id, category) =>
      log.info(s"Updating category $category")
      categories = categories + (id -> category)
      sender() ! OperationSuccess

    case RemoveCategory(category) =>
      log.info(s"Removing category $category")
      categories = categories - category.id
      sender() ! OperationSuccess
  }
}

trait CategoryJsonProtocol extends DefaultJsonProtocol {
  implicit val categoryFormat = jsonFormat3(Category)
}

object Main extends App with CategoryJsonProtocol {
  implicit val system = ActorSystem("foramSystem")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher
  import CategoryDB._

  val categoryDb = system.actorOf(Props[CategoryDB], "categoryDB")
  val categoryList = List(
    Category(1, "JavaScript", "Ask questions and share tips about JavaScript"),
    Category(2, "Java", "Ask questions and share tips about Java"),
    Category(3, "Scala", "Ask questions and share tips about Scala")
  )

  categoryList.foreach { category =>
    categoryDb ! AddCategory(category)
  }

  implicit val timeout = Timeout(2 seconds)

  val categoriesRoutes =
    pathPrefix("api" / "categories") {
      get {
        (path(IntNumber) | parameter('id.as[Int])) { id =>
          val categoryOptionFuture: Future[Option[Category]] = (categoryDb ? GetCategory(id)).mapTo[Option[Category]]
          complete(categoryOptionFuture)
        } ~
          pathEndOrSingleSlash {
            complete((categoryDb ? GetAllCategories).mapTo[List[Category]])
          }
      } ~
        post {
          entity(as[Category]) { category =>
            complete((categoryDb ? AddCategory(category)).map(_ => StatusCodes.OK))
          }
        } ~
        put {
          (path(IntNumber) | parameter('id.as[Int])) { id =>
            entity(as[Category]) { category =>
              complete((categoryDb ? UpdateCategory(id, category)).map(_ => StatusCodes.OK))
            }
          }
        } ~
        delete {
          entity(as[Category]) { category =>
            complete((categoryDb ? RemoveCategory(category)).map(_ => StatusCodes.OK))
          }
        }
    }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(categoriesRoutes)

  println(s"Server now online at http://localhost:8080")
}
