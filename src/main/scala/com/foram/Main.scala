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

case class Category(id: Int, name: String, user_id: Int, description: String)

case class Topic(id: Int, title: String, user_id: Int, category_id: Int)

case class User(id: Int, name: String, username: String, email: String)

object CategoryDB {
  case object GetAllCategories

  case class GetCategory(id: Int)

  case class AddCategory(category: Category)

  case class UpdateCategory(id: Int, category: Category)

  case class RemoveCategory(category: Category)

  case object OperationSuccess
}

object TopicDB {
  case object GetAllTopics

  case class GetTopic(id: Int)

  case class AddTopic(topic: Topic)

  case class UpdateTopic(id: Int, topic: Topic)

  case class RemoveTopic(topic: Topic)

  case object OperationSuccess
}

object UserDB {
  case object GetAllUsers

  case class GetUser(id: Int)

  case class AddUser(user: User)

  case class UpdateUser(id: Int, user: User)

  case class RemoveUser(user: User)

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
      log.info(s"Finding category with id: $id")
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

class TopicDB extends Actor with ActorLogging {

  import TopicDB._

  var topics: Map[Int, Topic] = Map()

  override def receive: Receive = {
    case GetAllTopics =>
      log.info(s"Searching for topics")
      sender() ! topics.values.toList

    case GetTopic(id) =>
      log.info(s"Finding topic with id: $id")
      sender() ! topics.get(id)

    case AddTopic(topic) =>
      log.info(s"Adding topic $topic")
      topics = topics + (topic.id -> topic)
      sender() ! OperationSuccess

    case UpdateTopic(id, topic) =>
      log.info(s"Updating topic $topic")
      topics = topics + (id -> topic)
      sender() ! OperationSuccess

    case RemoveTopic(topic) =>
      log.info(s"Removing topic $topic")
      topics = topics - topic.id
      sender() ! OperationSuccess
  }
}

class UserDB extends Actor with ActorLogging {

  import UserDB._

  var users: Map[Int, User] = Map()

  override def receive: Receive = {
    case GetAllUsers =>
      log.info(s"Searching for users")
      sender() ! users.values.toList

    case GetUser(id) =>
      log.info(s"Finding user with id: $id")
      sender() ! users.get(id)

    case AddUser(user) =>
      log.info(s"Adding user $user")
      users = users + (user.id -> user)
      sender() ! OperationSuccess

    case UpdateUser(id, user) =>
      log.info(s"Updating user with id: $id")
      users = users + (id -> user)
      sender() ! OperationSuccess

    case RemoveUser(user) =>
      log.info(s"Removing user $user")
      users = users - user.id
      sender() ! OperationSuccess
  }
}

trait CustomJsonProtocol extends DefaultJsonProtocol {
  implicit val categoryFormat = jsonFormat4(Category)
  implicit val topicFormat = jsonFormat4(Topic)
  implicit val userFormat = jsonFormat4(User)
}

object Main extends App with CustomJsonProtocol {
  implicit val system = ActorSystem("foramSystem")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher
  import CategoryDB._
  import TopicDB._
  import UserDB._

  val categoryDb = system.actorOf(Props[CategoryDB], "categoryDB")
  val topicDb = system.actorOf(Props[TopicDB], "topicDB")
  val userDb = system.actorOf(Props[UserDB], "userDB")

  val categoryList = List(
    Category(1, "JavaScript", 1, "Ask questions and share tips about JavaScript"),
    Category(2, "Java", 2, "Ask questions and share tips about Java"),
    Category(3, "Scala", 3, "Ask questions and share tips about Scala")
  )

  val topicList = List(
    Topic(1, "I have a question about React", 1, 1),
    Topic(2, "I have a question about Spring Boot", 1, 2),
    Topic(3, "I have a question about Akka", 1, 3)
  )

  val userList = List(
    User(1, "Quincy Lars", "quince", "qlars@example.com"),
    User(2, "Beatriz Stephanie", "beetz", "beetz@example.com"),
    User(3, "Naz Mahmood", "naziyah", "nazmahmood@example.com")
  )

  categoryList.foreach { category =>
    categoryDb ! AddCategory(category)
  }

  topicList.foreach { topic =>
    topicDb ! AddTopic(topic)
  }

  userList.foreach { user =>
    userDb ! AddUser(user)
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

  val topicsRoutes =
    pathPrefix("api" / "topics") {
      get {
        (path(IntNumber) | parameter('id.as[Int])) { id =>
          val topicOptionFuture: Future[Option[Topic]] = (topicDb ? GetTopic(id)).mapTo[Option[Topic]]
          complete(topicOptionFuture)
        } ~
          pathEndOrSingleSlash {
            complete((topicDb ? GetAllTopics).mapTo[List[Topic]])
          }
      } ~
        post {
          entity(as[Topic]) { topic =>
            complete((topicDb ? AddTopic(topic)).map(_ => StatusCodes.OK))
          }
        } ~
        put {
          (path(IntNumber) | parameter('id.as[Int])) { id =>
            entity(as[Topic]) { topic =>
              complete((topicDb ? UpdateTopic(id, topic)).map(_ => StatusCodes.OK))
            }
          }
        } ~
        delete {
          entity(as[Topic]) { topic =>
            complete((topicDb ? RemoveTopic(topic)).map(_ => StatusCodes.OK))
          }
        }
    }

  val usersRoutes =
    pathPrefix("api" / "users") {
      get {
        (path(IntNumber) | parameter('id.as[Int])) { id =>
          val userOptionFuture: Future[Option[User]] = (userDb ? GetUser(id)).mapTo[Option[User]]
          complete(userOptionFuture)
        } ~
          pathEndOrSingleSlash {
            complete((userDb ? GetAllUsers).mapTo[List[User]])
          }
      } ~
        post {
          entity(as[User]) { user =>
            complete((userDb ? AddUser(user)).map(_ => StatusCodes.OK))
          }
        } ~
        put {
          (path(IntNumber) | parameter('id.as[Int])) { id =>
            entity(as[User]) { user =>
              complete((userDb ? UpdateUser(id, user)).map(_ => StatusCodes.OK))
            }
          }
        } ~
        delete {
          entity(as[User]) { user =>
            complete((userDb ? RemoveUser(user)).map(_ => StatusCodes.OK))
          }
        }
    }

  val routes = categoriesRoutes ~ topicsRoutes ~ usersRoutes

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)

  println(s"Server now online at http://localhost:8080")
}
