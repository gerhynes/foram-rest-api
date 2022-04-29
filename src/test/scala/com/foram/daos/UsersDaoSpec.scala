package com.foram.daos

import com.foram.auth.Auth
import com.foram.models._
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UsersDaoSpec extends AnyWordSpec with Matchers with BeforeAndAfter with ScalaFutures {
  val db: PostgresProfile.backend.Database = Database.forConfig("testDB")

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  val users = TableQuery[UsersTable]
  val categories = TableQuery[CategoriesTable]
  val topics = TableQuery[TopicsTable]
  val posts = TableQuery[PostsTable]

  // Sample data
  val sampleCategory: Category = Category(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", "javascript", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem.", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleTopic: Topic = Topic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quince", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now())
  val samplePost: Post = Post(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleUser: User = User(UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", "quincy", "qlars@example.com", "password123", "admin", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleUpdatedUser: User = User(UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", "quincy", "qlars@gmail.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleNewUser: User = User(UUID.fromString("6a3f2a60-fbc1-4542-ac15-a4ceec2ad3de"), "Chris Hadfield", "cmdrHadfield", "chadfield@example.com", "password123", "admin", OffsetDateTime.now(), OffsetDateTime.now())

  def createSchemas(): Future[Unit] =
    db.run((users.schema ++ categories.schema ++ topics.schema ++ posts.schema).createIfNotExists)

  def dropSchemas(): Future[Unit] = {
    db.run((users.schema ++ categories.schema ++ topics.schema ++ posts.schema).dropIfExists)
  }

  def seedDB(): Future[Unit] = {
    val setup = DBIO.seq(
      users += sampleUser,
      categories += sampleCategory,
      topics += sampleTopic,
      posts += samplePost
    )
    db.run(setup)
  }

  //  Reset database contents before each test
  before {
    dropSchemas().flatMap(_ => createSchemas().flatMap(_ => seedDB())).futureValue
  }

  "UsersDao" should {
    "return a Seq of Users from findAll" in {
      val usersDao = new UsersDao(db)

      val usersFuture = usersDao.findAll

      usersFuture.futureValue shouldBe Seq(sampleUser)
    }

    "return an empty Seq from findAll if users table is empty" in {
      val usersDao = new UsersDao(db)

      // Delete sampleUser
      db.run(users.filter(_.id === sampleUser.id).delete)

      val usersFuture = usersDao.findAll

      usersFuture.futureValue shouldBe Seq()
      usersFuture.futureValue should !==(Seq(sampleUser))
    }

    "return a specific User from findById" in {
      val usersDao = new UsersDao(db)

      val userFuture = usersDao.findById(sampleUser.id)

      userFuture.futureValue shouldBe sampleUser
    }

    "return a specific User from findByUsername" in {
      val usersDao = new UsersDao(db)

      val userFuture = usersDao.findByUsername(sampleUser.username)

      userFuture.futureValue shouldBe sampleUser
    }

    "insert a User record from CreateUser" in {
      val usersDao = new UsersDao(db)

      val userIdFuture = usersDao.create(sampleNewUser)

      // Search for user by id
      // Cannot directly compare class instances as user password is randomly hashed before being saved to database
      val userFuture = db.run(users.filter(_.id === sampleNewUser.id).result.head)

      userIdFuture.futureValue shouldBe sampleNewUser.id
      userFuture.futureValue.id shouldBe sampleNewUser.id
    }

    "update a User record from UpdateUser" in {
      val usersDao = new UsersDao(db)

      val rowsAffectedFuture = usersDao.update(sampleUser.id, sampleUpdatedUser)

      val userFuture = db.run(users.filter(_.id === sampleUpdatedUser.id).result.head)

      rowsAffectedFuture.futureValue shouldBe 1
      userFuture.futureValue shouldBe sampleUpdatedUser
      userFuture.futureValue should !==(sampleUser)
    }

    "delete a User record from DeleteUser" in {
      val usersDao = new UsersDao(db)

      val rowsAffectedFuture = usersDao.delete(sampleUser.id)

      val usersFuture = db.run(users.sortBy(_.createdAt.asc).result)

      rowsAffectedFuture.futureValue shouldBe 1
      usersFuture.futureValue.toList should not contain sampleUser
    }
  }
}
