package com.foram

import com.foram.routes._
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.foram.actors._
import slick.basic.DatabasePublisher
import slick.jdbc.JdbcBackend._


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


  val db = Database.forConfig("mydb")

  // Hardcode values until database is connected
  val categoryList = List(
    Category(1, "JavaScript", "javascript", 1, "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem."),
    Category(2, "Java", "java" , 2, "Ask questions and share tips for Java, Spring, JUnit - anything to do with the Java ecosystem."),
    Category(3, "Scala", "scala", 3, "Ask questions and share tips for Scala, Akka, Play - anything to do with the Scala ecosystem."),
    Category(4, "Python", "python", 1, "Ask questions and share tips for Django, Pandas, PySpark - anything to do with the Python ecosystem."),
    Category(5, "Databases", "databases", 2, "Ask questions and share tips for SQL, Postgres, MongoDB - anything to do with databases."),
    Category(6, "DevOps", "devops", 3, "Ask questions and share tips for Docker, Kubernetes, Jenkins - anything to do with DevOps.")
  )

  val topicList = List(
    Topic(1, "I don't understand promises in JavaScript. Help!", "i-dont-understand-promise-in-javascript-help", 1, 1, "JavaScript"),
    Topic(2, "Can someone help me with Java multithreading?", "can-someone-help-me-with-java-multithreading", 1, 2, "Java"),
    Topic(3, "Are there any good Scala resources?", "are there any good scala resources", 2, 3, "Scala"),
    Topic(4, "How does useEffect work?", "how-does-useeffect-work", 2, 1, "JavaScript"),
    Topic(5, "Can someone help me set up Spring Boot?", "can-someone-help-me-set-up-spring-boot", 3, 2, "Java"),
    Topic(6, "Akka Persistence makes no sense, like none at all!", "akka-persistence-makes-no-sense-like-none-at-all", 3, 3, "Scala")
  )

  val userList = List(
    User(1, "Quincy Lars", "quince", "qlars@example.com"),
    User(2, "Beatriz Stephanie", "beetz", "beetz@example.com"),
    User(3, "Naz Mahmood", "naziyah", "nazmahmood@example.com")
  )

  val postList = List(
    Post(1, 1,"quince", 1, 1, "Lorem ipsum dolor sit amet, consectetur adipiscing elit"),
    Post(2, 2, "beetz", 1, 2, "Nam tempus metus non dui sollicitudin efficitur vel id mauris"),
    Post(3, 3, "naziyah", 1, 3, "Fusce tristique justo eu porta aliquet"),
    Post(4, 1, "quince", 2, 1, "Aenean placerat magna quis sollicitudin aliquet"),
    Post(5, 2, "beetz", 2, 2, "Quisque sed tellus sapien"),
    Post(6, 3, "naziyah", 2, 3, "Nullam ullamcorper tempor mi vel ornare"),
    Post(7, 1, "quince", 3, 1, "Quisque auctor nisi eget consectetur consequat"),
    Post(8, 2, "beetz", 3, 2, "Integer aliquam turpis id mi porttitor pellentesque"),
    Post(9, 3, "naziyah", 3, 3, "Fusce vel molestie neque, in pharetra nisl")
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
