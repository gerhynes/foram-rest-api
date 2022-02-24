package com.foram

import com.foram.routes._
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.foram.actors._

object Main extends App {
  implicit val system = ActorSystem("foramSystem")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  import com.foram.actors.CategoryDB._
  import com.foram.actors.TopicDB._
  import com.foram.actors.UserDB._
  import com.foram.actors.PostDB._

  val categoryDB = system.actorOf(Props[CategoryDB], "categoryDB")
  val topicDB = system.actorOf(Props[TopicDB], "topicDB")
  val userDB = system.actorOf(Props[UserDB], "userDB")
  val postDB = system.actorOf(Props[PostDB], "postDB")

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

  val postList = List(
    Post(1, 1, 1, 1, "Lorem ipsum dolor sit amet, consectetur adipiscing elit"),
    Post(2, 2, 1, 2, "Nam tempus metus non dui sollicitudin efficitur vel id mauris"),
    Post(3, 3, 1, 3, "Fusce tristique justo eu porta aliquet"),
    Post(4, 1, 2, 1, "Aenean placerat magna quis sollicitudin aliquet"),
    Post(5, 2, 2, 2, "Quisque sed tellus sapien"),
    Post(6, 3, 2, 3, "Nullam ullamcorper tempor mi vel ornare"),
    Post(7, 1, 3, 1, "Quisque auctor nisi eget consectetur consequat"),
    Post(8, 2, 3, 2, "Integer aliquam turpis id mi porttitor pellentesque"),
    Post(9, 3, 3, 3, "Fusce vel molestie neque, in pharetra nisl")
  )

  categoryList.foreach { category =>
    categoryDB ! AddCategory(category)
  }

  topicList.foreach { topic =>
    topicDB ! AddTopic(topic)
  }

  userList.foreach { user =>
    userDB ! AddUser(user)
  }

  postList.foreach { post =>
    postDB ! AddPost(post)
  }

  val categoryRouter = new CategoryRoutes()
  val topicRouter = new TopicRoutes()
  val postRouter = new PostRoutes()
  val userRouter = new UserRoutes()

  // Concat routes
  val routes = categoryRouter.categoryRoutes ~ topicRouter.topicRoutes ~ postRouter.postRoutes ~ userRouter.userRoutes

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)

  println(s"Server now online at http://localhost:8080")
}
