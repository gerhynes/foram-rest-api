package com.foram.seeds

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
  val userIds = List.fill(3)(randomUUID())
  val categoryIds = List.fill(6)(randomUUID())
  val topicIds = List.fill(9)(randomUUID())
  val postIds = List.fill(15)(randomUUID())

  try {
    // Seed database
    val setupAction = DBIO.seq(
      // Set up schemas
      (users.schema ++ categories.schema ++ topics.schema ++ posts.schema).create,

      // Insert users
      users += User(userIds(0), "Quincy Lars", "quincy", "qlars@example.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.now(), OffsetDateTime.now()),
      users += User(userIds(1), "Beatriz Stephanie", "beatz", "beatz@example.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.now(), OffsetDateTime.now()),
      users += User(userIds(2), "Naziyah Mahmood", "naziyah", "nazmahmood@example.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.now(), OffsetDateTime.now()),

      // Insert categories
      categories += Category(categoryIds(0), "JavaScript", "javascript", userIds(0), "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem.", OffsetDateTime.now(), OffsetDateTime.now()),
      categories += Category(categoryIds(1), "Java", "java", userIds(0), "Ask questions and share tips for Java, Spring, JUnit - anything to do with the Java ecosystem.", OffsetDateTime.now(), OffsetDateTime.now()),
      categories += Category(categoryIds(2), "Scala", "scala", userIds(1), "Ask questions and share tips for Scala, Akka, Play - anything to do with the Scala ecosystem.", OffsetDateTime.now(), OffsetDateTime.now()),
      categories += Category(categoryIds(3), "Python", "python", userIds(1), "Ask questions and share tips for Django, Pandas, PySpark - anything to do with the Python ecosystem.", OffsetDateTime.now(), OffsetDateTime.now()),
      categories += Category(categoryIds(4), "Databases", "databases", userIds(2), "Ask questions and share tips for SQL, Postgres, MongoDB - anything to do with databases.", OffsetDateTime.now(), OffsetDateTime.now()),
      categories += Category(categoryIds(5), "DevOps", "devops", userIds(2), "Ask questions and share tips for Docker, Kubernetes, Jenkins - anything to do with DevOps.", OffsetDateTime.now(), OffsetDateTime.now()),

      // Insert topics
      topics += Topic(topicIds(0), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", userIds(0), "quincy", categoryIds(0), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now()),
      topics += Topic(topicIds(1), "Can someone help me with Java multithreading?", "can-someone-help-me-with-java-multithreading", userIds(0), "quincy", categoryIds(1), "Java", OffsetDateTime.now(), OffsetDateTime.now()),
      topics += Topic(topicIds(2), "Are there any good Scala resources?", "are-there-any-good-scala-resources", userIds(1), "beatz", categoryIds(2), "Scala", OffsetDateTime.now(), OffsetDateTime.now()),
      topics += Topic(topicIds(3), "How does useEffect work?", "how-does-useeffect-work", userIds(1), "beatz", categoryIds(0), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now()),
      topics += Topic(topicIds(4), "Can someone help me set up Spring Boot?", "can-someone-help-me-set-up-spring-boot", userIds(2), "naziyah", categoryIds(1), "Java", OffsetDateTime.now(), OffsetDateTime.now()),
      topics += Topic(topicIds(5), "Akka Persistence makes no sense, like none at all!", "akka-persistence-makes-no-sense-like-none-at-all", userIds(2), "naziyah", categoryIds(2), "Scala", OffsetDateTime.now(), OffsetDateTime.now()),
      topics += Topic(topicIds(6), "How do you convert a Pandas DataFrame to a NumPy array?", "how-do-you-convert-a-pandas-dataframe-to-a-numpy-array", userIds(1), "beatz", categoryIds(3), "Python", OffsetDateTime.now(), OffsetDateTime.now()),
      topics += Topic(topicIds(7), "Postgres connection pool timeout issue", "postgres-connection-pool-timeout-issue", userIds(2), "naziyah", categoryIds(4), "Databases", OffsetDateTime.now(), OffsetDateTime.now()),
      topics += Topic(topicIds(8), "Setting up GitHub Actions with sbt", "setting-up-github-actions-with-sbt", userIds(2), "naziyah", categoryIds(5), "DevOps", OffsetDateTime.now(), OffsetDateTime.now()),

      // Insert posts
      posts += Post(postIds(0), userIds(0), "quincy", topicIds(0), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing elit", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(1), userIds(1), "beatz", topicIds(0), "i-dont-understand-promises-in-javascript-help", 2, "Nam tempus metus non dui sollicitudin efficitur vel id mauris", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(2), userIds(2), "naziyah", topicIds(0), "i-dont-understand-promises-in-javascript-help", 3, "Fusce tristique justo eu porta aliquet", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(3), userIds(0), "quincy", topicIds(1), "can-someone-help-me-with-java-multithreading", 1, "Aenean placerat magna quis sollicitudin aliquet", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(4), userIds(1), "beatz", topicIds(1), "can-someone-help-me-with-java-multithreading", 2, "Quisque sed tellus sapien", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(5), userIds(2), "naziyah", topicIds(1), "can-someone-help-me-with-java-multithreading", 3, "Nullam ullamcorper tempor mi vel ornare", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(6), userIds(0), "quincy", topicIds(2), "are-there-any-good-scala-resources", 1, "Quisque auctor nisi eget consectetur consequat", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(7), userIds(1), "beatz", topicIds(2), "are-there-any-good-scala-resources", 2, "Integer aliquam turpis id mi porttitor pellentesque", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(8), userIds(2), "naziyah", topicIds(2), "are-there-any-good-scala-resources", 3, "Fusce vel molestie neque, in pharetra nisl", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(9), userIds(1), "beatz", topicIds(3), "how-does-useeffect-work", 1, "Quisque auctor nisi eget consectetur consequat", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(10), userIds(2), "naziyah", topicIds(4), "can-someone-help-me-set-up-spring-boot", 1, "Integer aliquam turpis id mi porttitor pellentesque", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(11), userIds(2), "naziyah", topicIds(5), "akka-persistence-makes-no-sense-like-none-at-all", 1, "Fusce vel molestie neque, in pharetra nisl", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(12), userIds(1), "beatz", topicIds(6), "how-do-you-convert-a-pandas-dataframe-to-a-numpy-array", 1, "Quisque auctor nisi eget consectetur consequat", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(13), userIds(2), "naziyah", topicIds(7), "postgres-connection-pool-timeout-issue", 1, "Integer aliquam turpis id mi porttitor pellentesque", OffsetDateTime.now(), OffsetDateTime.now()),
      posts += Post(postIds(14), userIds(2), "naziyah", topicIds(8), "setting-up-github-actions-with-sbt", 1, "Fusce vel molestie neque, in pharetra nisl", OffsetDateTime.now(), OffsetDateTime.now())
    )

    val setupFuture = db.run(setupAction)

    setupFuture.onComplete {
      case Success(_) => println("Database seeded")
      case Failure(e) => e.printStackTrace()
    }
  } finally db.close
}
