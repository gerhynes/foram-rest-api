package com.foram.daos

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

class CategoriesDaoSpec extends AnyWordSpec with Matchers with BeforeAndAfter with ScalaFutures {
  val db: PostgresProfile.backend.Database = Database.forConfig("testDB")

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  val users = TableQuery[UsersTable]
  val categories = TableQuery[CategoriesTable]
  val topics = TableQuery[TopicsTable]
  val posts = TableQuery[PostsTable]

  // Sample data
  val sampleCategory: Category = Category(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", "javascript", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem.", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleTopic: Topic = Topic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quincy", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now())
  val samplePost: Post = Post(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleUser: User = User(UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", "quincy", "qlars@example.com", "password123", "admin", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleUpdatedCategory: Category = Category(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", "javascript", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Ask questions and share tips about core JavaScript, whether frontend frameworks such as React, or serverside tools like Express - anything to do with the JavaScript ecosystem.", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleNewCategory: Category = Category(UUID.fromString("90342838-093d-4101-bacc-a68f5764dae0"), "Testing", "testing", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Ask questions and share tips about Testing, from unit tests to end-to-end testing", OffsetDateTime.now(), OffsetDateTime.now())

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

  "CategoriesDao" should {
    "return a Seq of Categories from findAll" in {
      val categoriesDao = new CategoriesDao(db)

      val categoriesFuture = categoriesDao.findAll

      categoriesFuture.futureValue shouldBe Seq(sampleCategory)
    }

    "return an empty Seq from findAll if categories table is empty" in {
      val categoriesDao = new CategoriesDao(db)

      // Delete sampleTopic
      db.run(categories.filter(_.id === sampleCategory.id).delete)

      val categoriesFuture = categoriesDao.findAll

      categoriesFuture.futureValue shouldBe Seq()
      categoriesFuture.futureValue should !==(Seq(sampleCategory))
    }

    "return a specific Category from findById" in {
      val categoriesDao = new CategoriesDao(db)

      val categoryFuture = categoriesDao.findById(sampleCategory.id)

      categoryFuture.futureValue shouldBe sampleCategory
    }

    "insert a Category record from CreateCategory" in {
      val categoriesDao = new CategoriesDao(db)

      val categoryIdFuture = categoriesDao.create(sampleNewCategory)

      val categoryFuture = db.run(categories.filter(_.id === sampleNewCategory.id).result.head)

      categoryIdFuture.futureValue shouldBe sampleNewCategory.id
      categoryFuture.futureValue shouldBe sampleNewCategory
    }

    "update a Category record from UpdateCategory" in {
      val categoriesDao = new CategoriesDao(db)

      val rowsAffectedFuture = categoriesDao.update(sampleCategory.id, sampleUpdatedCategory)

      val categoryFuture = db.run(categories.filter(_.id === sampleUpdatedCategory.id).result.head)

      rowsAffectedFuture.futureValue shouldBe 1
      categoryFuture.futureValue shouldBe sampleUpdatedCategory
      categoryFuture.futureValue should !==(sampleCategory)
    }

    "delete a Category record from DeleteCategory" in {
      val categoriesDao = new CategoriesDao(db)

      val rowsAffectedFuture = categoriesDao.delete(sampleCategory.id)

      val categoriesFuture = db.run(categories.sortBy(_.createdAt.asc).result)

      rowsAffectedFuture.futureValue shouldBe 1
      categoriesFuture.futureValue.toList should not contain sampleCategory
    }
  }
}
