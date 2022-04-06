package com.foram.utils

import com.foram.auth.Auth
import com.foram.models._
import slick.jdbc.PostgresProfile.api._

import java.util.UUID.randomUUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import java.time.OffsetDateTime

// Seeds database with sample tables
object SeedDB extends App {
  // Database connection
  val db = Database.forConfig("postgresDB")

  // Query interfaces for tables
  val users = TableQuery[UsersTable]
  val categories = TableQuery[CategoriesTable]
  val topics = TableQuery[TopicsTable]
  val posts = TableQuery[PostsTable]

  // Generate random ids
  val userIds = (randomUUID(), randomUUID(), randomUUID())
  val categoryIds = (randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID())
  val topicIds = (randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID())
  val postIds = (randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID())

  try {
    // Seed database
    val setupAction = DBIO.seq(
      // Set up schemas
      (users.schema ++ categories.schema ++ topics.schema ++ posts.schema).createIfNotExists,

      // Insert users
      users += User(userIds._1, "Quincy Lars", "quince", "qlars@example.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.parse("2022-02-20T06:30:00.166z"), OffsetDateTime.parse("2022-02-20T06:30:00.166z")),
      users += User(userIds._2, "Beatriz Stephanie", "beetz", "beetz@example.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.parse("2022-02-20T06:35:00.155z"), OffsetDateTime.parse("2022-02-20T06:35:00.155z")),
      users += User(userIds._3, "Naz Mahmood", "naziyah", "nazmahmood@example.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.parse("2022-02-20T06:45:00.155z"), OffsetDateTime.parse("2022-02-20T06:45:00.155z")),

      // Insert categories
      categories += Category(categoryIds._1, "JavaScript", "javascript", userIds._1, "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem.", OffsetDateTime.parse("2022-02-20T07:30:00.155z"), OffsetDateTime.parse("2022-02-20T07:30:00.155z")),
      categories += Category(categoryIds._2, "Java", "java", userIds._2, "Ask questions and share tips for Java, Spring, JUnit - anything to do with the Java ecosystem.", OffsetDateTime.parse("2022-02-20T08:15:00.155z"), OffsetDateTime.parse("2022-02-20T08:15:00.155z")),
      categories += Category(categoryIds._3, "Scala", "scala", userIds._3, "Ask questions and share tips for Scala, Akka, Play - anything to do with the Scala ecosystem.", OffsetDateTime.parse("2022-02-20T10:25:00.155z"), OffsetDateTime.parse("2022-02-20T10:25:00.155z")),
      categories += Category(categoryIds._4, "Python", "python", userIds._1, "Ask questions and share tips for Django, Pandas, PySpark - anything to do with the Python ecosystem.", OffsetDateTime.parse("2022-02-20T09:30:00.155z"), OffsetDateTime.parse("2022-02-20T09:30:00.155z")),
      categories += Category(categoryIds._5, "Databases", "databases", userIds._2, "Ask questions and share tips for SQL, Postgres, MongoDB - anything to do with databases.", OffsetDateTime.parse("2022-02-20T11:30:00.155z"), OffsetDateTime.parse("2022-02-20T11:30:00.155z")),
      categories += Category(categoryIds._6, "DevOps", "devops", userIds._3, "Ask questions and share tips for Docker, Kubernetes, Jenkins - anything to do with DevOps.", OffsetDateTime.parse("2022-02-20T11:35:00.155z"), OffsetDateTime.parse("2022-02-20T11:35:00.155z")),

      // Insert topics
      topics += Topic(topicIds._1, "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", userIds._1, "quince", categoryIds._1, "JavaScript", OffsetDateTime.parse("2022-02-20T09:30:00.155z"), OffsetDateTime.parse("2022-02-20T09:30:00.155z")),
      topics += Topic(topicIds._2, "Can someone help me with Java multithreading?", "can-someone-help-me-with-java-multithreading", userIds._1, "quince", categoryIds._2, "Java", OffsetDateTime.parse("2022-02-20T10:30:00.155z"), OffsetDateTime.parse("2022-02-20T10:30:00.155z")),
      topics += Topic(topicIds._3, "Are there any good Scala resources?", "are-there-any-good-scala-resources", userIds._2, "beetz", categoryIds._3, "Scala", OffsetDateTime.parse("2022-02-20T11:15:00.155z"), OffsetDateTime.parse("2022-02-20T11:15:00.155z")),
      topics += Topic(topicIds._4, "How does useEffect work?", "how-does-useeffect-work", userIds._2, "beetz", categoryIds._1, "JavaScript", OffsetDateTime.parse("2022-02-20T11:30:00.155z"), OffsetDateTime.parse("2022-02-20T11:30:00.155z")),
      topics += Topic(topicIds._5, "Can someone help me set up Spring Boot?", "can-someone-help-me-set-up-spring-boot", userIds._3, "naziyah", categoryIds._2, "Java", OffsetDateTime.parse("2022-02-20T11:45:00.155z"), OffsetDateTime.parse("2022-02-20T11:45:00.155z")),
      topics += Topic(topicIds._6, "Akka Persistence makes no sense, like none at all!", "akka-persistence-makes-no-sense-like-none-at-all", userIds._3, "naziyah", categoryIds._3, "Scala", OffsetDateTime.parse("2022-02-20T11:55:00.155z"), OffsetDateTime.parse("2022-02-20T11:55:00.155z")),

      // Insert posts
      posts += Post(postIds._1, userIds._1, "quince", topicIds._1, "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing elit", OffsetDateTime.parse("2022-02-20T09:30:00.155z"), OffsetDateTime.parse("2022-02-20T09:30:00.155z")),
      posts += Post(postIds._2, userIds._2, "beetz", topicIds._1, "i-dont-understand-promises-in-javascript-help", 2, "Nam tempus metus non dui sollicitudin efficitur vel id mauris", OffsetDateTime.parse("2022-02-20T09:32:00.155z"), OffsetDateTime.parse("2022-02-20T09:32:00.155z")),
      posts += Post(postIds._3, userIds._3, "naziyah", topicIds._1, "i-dont-understand-promises-in-javascript-help", 3, "Fusce tristique justo eu porta aliquet", OffsetDateTime.parse("2022-02-20T09:36:00.155z"), OffsetDateTime.parse("2022-02-20T09:36:00.155z")),
      posts += Post(postIds._4, userIds._1, "quince", topicIds._2, "can-someone-help-me-with-java-multithreading", 1, "Aenean placerat magna quis sollicitudin aliquet", OffsetDateTime.parse("2022-02-20T10:30:00.155z"), OffsetDateTime.parse("2022-02-20T10:30:00.155z")),
      posts += Post(postIds._5, userIds._2, "beetz", topicIds._2, "can-someone-help-me-with-java-multithreading", 2, "Quisque sed tellus sapien", OffsetDateTime.parse("2022-02-20T10:45:00.155z"), OffsetDateTime.parse("2022-02-20T10:45:00.155z")),
      posts += Post(postIds._6, userIds._3, "naziyah", topicIds._2, "can-someone-help-me-with-java-multithreading", 3, "Nullam ullamcorper tempor mi vel ornare", OffsetDateTime.parse("2022-02-20T10:47:00.155z"), OffsetDateTime.parse("2022-02-20T10:47:00.155z")),
      posts += Post(postIds._7, userIds._1, "quince", topicIds._3, "are-there-any-good-scala-resources", 1, "Quisque auctor nisi eget consectetur consequat", OffsetDateTime.parse("2022-02-20T11:15:00.155z"), OffsetDateTime.parse("2022-02-20T11:15:00.155z")),
      posts += Post(postIds._8, userIds._2, "beetz", topicIds._3, "are-there-any-good-scala-resources", 2, "Integer aliquam turpis id mi porttitor pellentesque", OffsetDateTime.parse("2022-02-20T11:21:00.155z"), OffsetDateTime.parse("2022-02-20T11:21:00.155z")),
      posts += Post(postIds._9, userIds._3, "naziyah", topicIds._3, "are-there-any-good-scala-resources", 3, "Fusce vel molestie neque, in pharetra nisl", OffsetDateTime.parse("2022-02-20T11:33:00.155z"), OffsetDateTime.parse("2022-02-20T11:33:00.155z"))
    )

    val setupFuture = db.run(setupAction)

    //    Await.result(setupFuture, Duration.Inf)
    setupFuture.onComplete {
      case Success(success) => println("Db seeded")
      case Failure(e) => e.printStackTrace()
    }
  } finally db.close
}
