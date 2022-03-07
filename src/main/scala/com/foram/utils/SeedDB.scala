package com.foram.utils

import com.foram.models._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

// Seeds database with sample tables
class SeedDB {
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
      users += User(1, "Quincy Lars", "quince", "qlars@example.com"),
      users += User(2, "Beatriz Stephanie", "beetz", "beetz@example.com"),
      users += User(3, "Naz Mahmood", "naziyah", "nazmahmood@example.com"),

      // Insert categories
      categories += Category(1, "JavaScript", "javascript", 1, "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem."),
      categories += Category(2, "Java", "java", 2, "Ask questions and share tips for Java, Spring, JUnit - anything to do with the Java ecosystem."),
      categories += Category(3, "Scala", "scala", 3, "Ask questions and share tips for Scala, Akka, Play - anything to do with the Scala ecosystem."),
      categories += Category(4, "Python", "python", 1, "Ask questions and share tips for Django, Pandas, PySpark - anything to do with the Python ecosystem."),
      categories += Category(5, "Databases", "databases", 2, "Ask questions and share tips for SQL, Postgres, MongoDB - anything to do with databases."),
      categories += Category(6, "DevOps", "devops", 3, "Ask questions and share tips for Docker, Kubernetes, Jenkins - anything to do with DevOps."),

      // Insert topics
      topics += Topic(1, "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", 1, "quince", 1, "JavaScript"),
      topics += Topic(2, "Can someone help me with Java multithreading?", "can-someone-help-me-with-java-multithreading", 1, "quince", 2, "Java"),
      topics += Topic(3, "Are there any good Scala resources?", "are-there-any-good-scala-resources", 2, "beetz", 3, "Scala"),
      topics += Topic(4, "How does useEffect work?", "how-does-useeffect-work", 2, "beetz", 1, "JavaScript"),
      topics += Topic(5, "Can someone help me set up Spring Boot?", "can-someone-help-me-set-up-spring-boot", 3, "naziyah", 2, "Java"),
      topics += Topic(6, "Akka Persistence makes no sense, like none at all!", "akka-persistence-makes-no-sense-like-none-at-all", 3, "naziyah", 3, "Scala"),

      // Insert posts
      posts += Post(1, 1, "quince", 1, "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing elit"),
      posts += Post(2, 2, "beetz", 1, "i-dont-understand-promises-in-javascript-help", 2, "Nam tempus metus non dui sollicitudin efficitur vel id mauris"),
      posts += Post(3, 3, "naziyah", 1, "i-dont-understand-promises-in-javascript-help", 3, "Fusce tristique justo eu porta aliquet"),
      posts += Post(4, 1, "quince", 2, "can-someone-help-me-with-java-multithreading", 1, "Aenean placerat magna quis sollicitudin aliquet"),
      posts += Post(5, 2, "beetz", 2, "can-someone-help-me-with-java-multithreading", 2, "Quisque sed tellus sapien"),
      posts += Post(6, 3, "naziyah", 2, "can-someone-help-me-with-java-multithreading", 3, "Nullam ullamcorper tempor mi vel ornare"),
      posts += Post(7, 1, "quince", 3, "are-there-any-good-scala-resources", 1, "Quisque auctor nisi eget consectetur consequat"),
      posts += Post(8, 2, "beetz", 3, "are-there-any-good-scala-resources", 2, "Integer aliquam turpis id mi porttitor pellentesque"),
      posts += Post(9, 3, "naziyah", 3, "are-there-any-good-scala-resources", 3, "Fusce vel molestie neque, in pharetra nisl")
    )

    val setupFuture = db.run(setupAction)

    Await.result(setupFuture, Duration.Inf)
  } finally db.close
}
