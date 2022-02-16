package com.foram

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Future
import scala.concurrent.duration._

import spray.json._

case class Category(name: String, description: String)

object CategoryDB {
  case class CreateCategory(category: Category)

  case class CategoryCreated(id: Int)

  case class FindCategory(id: Int)

  case object FindAllCategories
}

class CategoryDB extends Actor with ActorLogging {

  import CategoryDB._

  var categories: Map[Int, Category] = Map()
  var currentCategoryId: Int = 0

  override def receive: Receive = {
    case FindAllCategories =>
      log.info(s"Searching for categories")
      sender() ! categories.values.toList

    case FindCategory(id) =>
      log.info(s"Finding category by id: $id")
      sender() ! categories.get(id)

    case CreateCategory(category) =>
      log.info(s"Adding category $category with id $currentCategoryId")
      categories = categories + (currentCategoryId -> category)
      sender() ! CategoryCreated(currentCategoryId)
      currentCategoryId += 1
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
    categoryDb ! CreateCategory(category)
  }

  implicit val timeout = Timeout(2 seconds)

  val routes =
    path("api" / "categories") {
      parameter('id.as[Int]) { categoryId =>
        get {
          val categoryFuture: Future[Option[Category]] = (categoryDb ? FindCategory(categoryId)).mapTo[Option[Category]]
          val entityFuture = categoryFuture.map { categoryOption =>
            HttpEntity(
              ContentTypes.`application/json`,
              categoryOption.toJson.prettyPrint
            )
          }
          complete(entityFuture)
        }
      } ~
      get {
        val categoriesFuture: Future[List[Category]] = (categoryDb ? FindAllCategories).mapTo[List[Category]]
        val entityFuture = categoriesFuture.map { categories =>
          HttpEntity(
            ContentTypes.`application/json`,
            categories.toJson.prettyPrint
          )
        }
        complete(entityFuture)
      }
    } ~
      path("api" / "categories" / IntNumber) { categoryId =>
        get {
          val categoryFuture: Future[Option[Category]] = (categoryDb ? FindCategory(categoryId)).mapTo[Option[Category]]
          val entityFuture = categoryFuture.map { categoryOption =>
            HttpEntity(
              ContentTypes.`application/json`,
              categoryOption.toJson.prettyPrint
            )
          }
          complete(entityFuture)
        }
      }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)

  println(s"Server now online at http://localhost:8080")
}
