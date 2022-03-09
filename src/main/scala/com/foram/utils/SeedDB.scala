package com.foram.utils

import com.foram.models._
import slick.jdbc.PostgresProfile.api._

import java.util.UUID.randomUUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

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
  val user_id1 = randomUUID()
  val user_id2 = randomUUID()
  val user_id3 = randomUUID()

  val category_id1 = randomUUID()
  val category_id2 = randomUUID()
  val category_id3 = randomUUID()
  val category_id4 = randomUUID()
  val category_id5 = randomUUID()
  val category_id6 = randomUUID()

  val topic_id1 = randomUUID()
  val topic_id2 = randomUUID()
  val topic_id3 = randomUUID()
  val topic_id4 = randomUUID()
  val topic_id5 = randomUUID()
  val topic_id6 = randomUUID()

  val post_id1 = randomUUID()
  val post_id2 = randomUUID()
  val post_id3 = randomUUID()
  val post_id4 = randomUUID()
  val post_id5 = randomUUID()
  val post_id6 = randomUUID()
  val post_id7 = randomUUID()
  val post_id8 = randomUUID()
  val post_id9 = randomUUID()

  try {
    // Seed database
    val setupAction = DBIO.seq(
      // Set up schemas
      (users.schema ++ categories.schema ++ topics.schema ++ posts.schema).createIfNotExists,

      // Insert users
      users += User(user_id1, "Quincy Lars", "quince", "qlars@example.com"),
      users += User(user_id2, "Beatriz Stephanie", "beetz", "beetz@example.com"),
      users += User(user_id3, "Naz Mahmood", "naziyah", "nazmahmood@example.com"),

      // Insert categories
      categories += Category(category_id1, "JavaScript", "javascript", user_id1, "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem."),
      categories += Category(category_id2, "Java", "java", user_id2, "Ask questions and share tips for Java, Spring, JUnit - anything to do with the Java ecosystem."),
      categories += Category(category_id3, "Scala", "scala", user_id3, "Ask questions and share tips for Scala, Akka, Play - anything to do with the Scala ecosystem."),
      categories += Category(category_id4, "Python", "python", user_id1, "Ask questions and share tips for Django, Pandas, PySpark - anything to do with the Python ecosystem."),
      categories += Category(category_id5, "Databases", "databases", user_id2, "Ask questions and share tips for SQL, Postgres, MongoDB - anything to do with databases."),
      categories += Category(category_id6, "DevOps", "devops", user_id3, "Ask questions and share tips for Docker, Kubernetes, Jenkins - anything to do with DevOps."),

      // Insert topics
      topics += Topic(topic_id1, "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", user_id1, "quince", category_id1, "JavaScript"),
      topics += Topic(topic_id2, "Can someone help me with Java multithreading?", "can-someone-help-me-with-java-multithreading", user_id1, "quince", category_id2, "Java"),
      topics += Topic(topic_id3, "Are there any good Scala resources?", "are-there-any-good-scala-resources", user_id2, "beetz", category_id3, "Scala"),
      topics += Topic(topic_id4, "How does useEffect work?", "how-does-useeffect-work", user_id2, "beetz", category_id1, "JavaScript"),
      topics += Topic(topic_id5, "Can someone help me set up Spring Boot?", "can-someone-help-me-set-up-spring-boot", user_id3, "naziyah", category_id2, "Java"),
      topics += Topic(topic_id6, "Akka Persistence makes no sense, like none at all!", "akka-persistence-makes-no-sense-like-none-at-all", user_id3, "naziyah", category_id3, "Scala"),

      // Insert posts
      posts += Post(post_id1, user_id1, "quince", topic_id1, "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing elit"),
      posts += Post(post_id2, user_id2, "beetz", topic_id1, "i-dont-understand-promises-in-javascript-help", 2, "Nam tempus metus non dui sollicitudin efficitur vel id mauris"),
      posts += Post(post_id3, user_id3, "naziyah", topic_id1, "i-dont-understand-promises-in-javascript-help", 3, "Fusce tristique justo eu porta aliquet"),
      posts += Post(post_id4, user_id1, "quince", topic_id2, "can-someone-help-me-with-java-multithreading", 1, "Aenean placerat magna quis sollicitudin aliquet"),
      posts += Post(post_id5, user_id2, "beetz", topic_id2, "can-someone-help-me-with-java-multithreading", 2, "Quisque sed tellus sapien"),
      posts += Post(post_id6, user_id3, "naziyah", topic_id2, "can-someone-help-me-with-java-multithreading", 3, "Nullam ullamcorper tempor mi vel ornare"),
      posts += Post(post_id7, user_id1, "quince", topic_id3, "are-there-any-good-scala-resources", 1, "Quisque auctor nisi eget consectetur consequat"),
      posts += Post(post_id8, user_id2, "beetz", topic_id3, "are-there-any-good-scala-resources", 2, "Integer aliquam turpis id mi porttitor pellentesque"),
      posts += Post(post_id9, user_id3, "naziyah", topic_id3, "are-there-any-good-scala-resources", 3, "Fusce vel molestie neque, in pharetra nisl")
    )

    val setupFuture = db.run(setupAction)

    //    Await.result(setupFuture, Duration.Inf)
    setupFuture.onComplete {
      case Success(success) => println("Db seeded")
      case Failure(e) => e.printStackTrace()
    }
  } finally db.close
}
