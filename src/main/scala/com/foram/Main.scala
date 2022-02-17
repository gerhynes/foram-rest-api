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
import scala.collection.mutable.Map

import scala.concurrent.Future
import scala.concurrent.duration._
import spray.json._

case class Category(name: String, description: String)

object CategoryDB {
  case object GetAllCategories
  case class GetCategory(name: String)
  case class AddCategory(category: Category)
  case class UpdateCategory(name: String, category: Category)
  case class RemoveCategory(category: Category)
  case object OperationSuccess
}

class CategoryDB extends Actor with ActorLogging {
  import CategoryDB._

  var categories: Map[String, Category] = Map()

  override def receive: Receive = {
    case GetAllCategories =>
      log.info(s"Searching for categories")
      sender() ! categories.values.toList

    case GetCategory(name) =>
      log.info(s"Finding category with name: $name")
      sender() ! categories.get(name)

    case AddCategory(category) =>
      log.info(s"Adding category $category")
      categories = categories + (category.name -> category)
      sender() ! OperationSuccess

    case UpdateCategory(name, category) =>
      log.info(s"Updating category $category")
      categories = categories + (name -> category)
      sender() ! OperationSuccess

    case RemoveCategory(category) =>
      log.info(s"Removing category $category")
      categories = categories - category.name
      sender() ! OperationSuccess
  }
}

trait CategoryJsonProtocol extends DefaultJsonProtocol {
  implicit val categoryFormat = jsonFormat2(Category)
}

object Main extends App with CategoryJsonProtocol {
  implicit val system = ActorSystem("foramSystem")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher
  import CategoryDB._

  val categoryDb = system.actorOf(Props[CategoryDB], "categoryDB")
  val categoryList = List(
    Category("JavaScript", "Ask questions and share tips about JavaScript"),
    Category("Java", "Ask questions and share tips about Java"),
    Category("Scala", "Ask questions and share tips about Scala")
  )

  categoryList.foreach { category =>
    categoryDb ! AddCategory(category)
  }

  implicit val timeout = Timeout(2 seconds)

  val categoriesRoutes =
    pathPrefix("api" / "categories") {
      get {
        (path(Segment) | parameter('name)) { name =>
          val categoryOptionFuture: Future[Option[Category]] = (categoryDb ? GetCategory(name)).mapTo[Option[Category]]
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
          (path(Segment) | parameter('name)) { name =>
            entity(as[Category]) { category =>
              complete((categoryDb ? UpdateCategory(name, category)).map(_ => StatusCodes.OK))
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
