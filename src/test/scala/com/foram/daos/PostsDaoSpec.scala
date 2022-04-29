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

class PostsDaoSpec extends AnyWordSpec with Matchers with BeforeAndAfter with ScalaFutures {
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
  val sampleUpdatedPost: Post = Post(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Excepteur sint occaecat cupidatat non proident", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleNewPost: Post = Post(UUID.fromString("a7dc08d5-c180-4809-8843-903f66355a3b"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 2, "Vitae suscipit tellus mauris a diam maecenas sed enim ut.", OffsetDateTime.now(), OffsetDateTime.now())


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

  "PostsDao" should {
    "return a Seq of Posts from findAll" in {
      val postsDao = new PostsDao(db)

      val postsFuture = postsDao.findAll

      postsFuture.futureValue shouldBe Seq(samplePost)
    }

    "return an empty Seq from findAll if posts table is empty" in {
      val postsDao = new PostsDao(db)

      // Delete samplePost
      db.run(posts.filter(_.id === samplePost.id).delete)

      val postsFuture = postsDao.findAll

      postsFuture.futureValue shouldBe Seq()
      postsFuture.futureValue should !==(Seq(samplePost))
    }

    "return a specific Post from findById" in {
      val postsDao = new PostsDao(db)

      val postFuture = postsDao.findById(samplePost.id)

      postFuture.futureValue shouldBe samplePost
    }

    "return a Seq of Posts from findByTopicID" in {
      val postsDao = new PostsDao(db)

      val postFuture = postsDao.findByTopicID(samplePost.topic_id)

      postFuture.futureValue shouldBe Seq(samplePost)
    }

    "return a Seq of Posts from findByUserID" in {
      val postsDao = new PostsDao(db)

      val postFuture = postsDao.findByUserID(samplePost.user_id)

      postFuture.futureValue shouldBe Seq(samplePost)
    }

    "return a Seq of Posts from findByUsername" in {
      val postsDao = new PostsDao(db)

      val postFuture = postsDao.findByUsername(samplePost.username)

      postFuture.futureValue shouldBe Seq(samplePost)
    }

    "insert a Post record from CreatePost" in {
      val postsDao = new PostsDao(db)

      val postIdFuture = postsDao.create(sampleNewPost)

      val postFuture = db.run(posts.filter(_.id === sampleNewPost.id).result.head)

      postIdFuture.futureValue shouldBe sampleNewPost.id
      postFuture.futureValue shouldBe sampleNewPost
    }

    "update a Post record from UpdatePost" in {
      val postsDao = new PostsDao(db)

      val rowsAffectedFuture = postsDao.update(samplePost.id, sampleUpdatedPost)

      val postFuture = db.run(posts.filter(_.id === sampleUpdatedPost.id).result.head)

      rowsAffectedFuture.futureValue shouldBe 1
      postFuture.futureValue shouldBe sampleUpdatedPost
      postFuture.futureValue should !==(samplePost)
    }

    "delete a Post record from DeletePost" in {
      val postsDao = new PostsDao(db)

      val rowsAffectedFuture = postsDao.delete(samplePost.id)

      val postsFuture = db.run(posts.sortBy(_.createdAt.asc).result)

      rowsAffectedFuture.futureValue shouldBe 1
      postsFuture.futureValue.toList should not contain samplePost
    }
  }
}
