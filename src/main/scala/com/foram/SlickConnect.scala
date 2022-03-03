package com.foram

import com.foram.db._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import slick.basic.DatabasePublisher
import slick.jdbc.PostgresProfile.api._

object SlickConnect extends App{
  val db = Database.forConfig("postgresDB")

  try {
    val users = TableQuery[Users]

    val setupAction = DBIO.seq(
      (users.schema).create,

      users += (1, "User One", "user1", "user1@example.com"),
      users += (2, "User Two", "user2", "user2@example.com"),
      users += (3, "User Three", "user3", "user3@example.com")
    )

    val setupFuture = db.run(setupAction)

    Await.result(setupFuture, Duration.Inf)
  } finally db.close
}
