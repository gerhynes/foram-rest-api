package com.foram.utils

import com.foram.models._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

// Seeds database with sample tables
object SeedDB extends App{
  // Database connection
  val db = Database.forConfig("postgresDB")

  // Query interfaces for tables
  val users = TableQuery[UsersTable]
  val categories = TableQuery[CategoriesTable]
  val topics = TableQuery[TopicsTable]
  val posts = TableQuery[PostsTable]

  try {
    // Seed database
    val setupAction = DBIO.seq(
      // Set up schemas
      (users.schema ++ categories.schema ++ topics.schema ++ posts.schema).createIfNotExists,

      // Insert users
      users += User(None, "Quincy Lars", "quince", "qlars@example.com"),
      users += User(None, "Beatriz Stephanie", "beetz", "beetz@example.com"),
      users += User(None, "Naz Mahmood", "naziyah", "nazmahmood@example.com"),

      // Insert categories
      categories += Category(None, "JavaScript", "javascript", 1, "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem."),
      categories += Category(None, "Java", "java", 2, "Ask questions and share tips for Java, Spring, JUnit - anything to do with the Java ecosystem."),
      categories += Category(None, "Scala", "scala", 3, "Ask questions and share tips for Scala, Akka, Play - anything to do with the Scala ecosystem."),
      categories += Category(None, "Python", "python", 1, "Ask questions and share tips for Django, Pandas, PySpark - anything to do with the Python ecosystem."),
      categories += Category(None, "Databases", "databases", 2, "Ask questions and share tips for SQL, Postgres, MongoDB - anything to do with databases."),
      categories += Category(None, "DevOps", "devops", 3, "Ask questions and share tips for Docker, Kubernetes, Jenkins - anything to do with DevOps."),

      // Insert topics
      topics += Topic(None, "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", 1, "quince", 1, "JavaScript"),
      topics += Topic(None, "Can someone help me with Java multithreading?", "can-someone-help-me-with-java-multithreading", 1, "quince", 2, "Java"),
      topics += Topic(None, "Are there any good Scala resources?", "are-there-any-good-scala-resources", 2, "beetz", 3, "Scala"),
      topics += Topic(None, "How does useEffect work?", "how-does-useeffect-work", 2, "beetz", 1, "JavaScript"),
      topics += Topic(None, "Can someone help me set up Spring Boot?", "can-someone-help-me-set-up-spring-boot", 3, "naziyah", 2, "Java"),
      topics += Topic(None, "Akka Persistence makes no sense, like none at all!", "akka-persistence-makes-no-sense-like-none-at-all", 3, "naziyah", 3, "Scala"),

      // Insert posts
      posts += Post(None, 1, "quince", 1, "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing elit"),
      posts += Post(None, 2, "beetz", 1, "i-dont-understand-promises-in-javascript-help", 2, "Nam tempus metus non dui sollicitudin efficitur vel id mauris"),
      posts += Post(None, 3, "naziyah", 1, "i-dont-understand-promises-in-javascript-help", 3, "Fusce tristique justo eu porta aliquet"),
      posts += Post(None, 1, "quince", 2, "can-someone-help-me-with-java-multithreading", 1, "Aenean placerat magna quis sollicitudin aliquet"),
      posts += Post(None, 2, "beetz", 2, "can-someone-help-me-with-java-multithreading", 2, "Quisque sed tellus sapien"),
      posts += Post(None, 3, "naziyah", 2, "can-someone-help-me-with-java-multithreading", 3, "Nullam ullamcorper tempor mi vel ornare"),
      posts += Post(None, 1, "quince", 3, "are-there-any-good-scala-resources", 1, "Quisque auctor nisi eget consectetur consequat"),
      posts += Post(None, 2, "beetz", 3, "are-there-any-good-scala-resources", 2, "Integer aliquam turpis id mi porttitor pellentesque"),
      posts += Post(None, 3, "naziyah", 3, "are-there-any-good-scala-resources", 3, "Fusce vel molestie neque, in pharetra nisl")
    )

    val setupFuture = db.run(setupAction)

    Await.result(setupFuture, Duration.Inf)
  } finally db.close
}
