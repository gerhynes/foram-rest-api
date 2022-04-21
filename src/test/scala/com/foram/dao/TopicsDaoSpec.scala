package com.foram.dao

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

class TopicsDaoSpec extends AnyWordSpec with Matchers with BeforeAndAfter with ScalaFutures {
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
  val sampleUpdatedTopic: Topic = Topic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "Can anyone explain promises in JavaScript?", "can-anyone-explain-promises-in-javascript", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quincy", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleNewTopic: Topic = Topic(UUID.fromString("fff3ee53-216c-4eac-bde6-6664a4eb2db8"), "React state not updating correctly", "react-state-not-updating-correctly", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quincy", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now())

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

  "TopicsDao" should {
    "return a Seq of Topics from findAll" in {
      val topicsDao = new TopicsDao(db)

      val topicsFuture = topicsDao.findAll

      topicsFuture.futureValue shouldBe Seq(sampleTopic)
    }

    "return an empty Seq from findAll if topics table is empty" in {
      val topicsDao = new TopicsDao(db)

      // Delete sampleTopic
      db.run(topics.filter(_.id === sampleTopic.id).delete)

      val topicsFuture = topicsDao.findAll

      topicsFuture.futureValue shouldBe Seq()
      topicsFuture.futureValue should !==(Seq(sampleTopic))
    }

    "return a Seq of Topics ordered by created_at from findLatest" in {
      val topicsDao = new TopicsDao(db)

      db.run(topics.returning(topics.map(_.id)) += sampleNewTopic)

      val topicsFuture = topicsDao.findLatest

      topicsFuture.futureValue shouldBe Seq(sampleNewTopic, sampleTopic)
      topicsFuture.futureValue.toList.head.created_at shouldBe >(topicsFuture.futureValue.toList(1).created_at)
    }

    "return a specific Topic from findById" in {
      val topicsDao = new TopicsDao(db)

      val topicFuture = topicsDao.findById(sampleTopic.id)

      topicFuture.futureValue shouldBe sampleTopic
    }

    "return a Seq of Posts from findByUsername" in {
      val postsDao = new PostsDao(db)

      val postFuture = postsDao.findByUsername(samplePost.username)

      postFuture.futureValue shouldBe Seq(samplePost)
    }

    "return a Seq of Topics from findByUserID" in {
      val topicsDao = new TopicsDao(db)

      val topicFuture = topicsDao.findByUserID(sampleTopic.user_id)

      topicFuture.futureValue shouldBe Seq(sampleTopic)
    }

    "return a Seq of Topics from findByCategoryID" in {
      val topicsDao = new TopicsDao(db)

      val topicFuture = topicsDao.findByCategoryID(sampleTopic.category_id)

      topicFuture.futureValue shouldBe Seq(sampleTopic)
    }

    "insert a Topic record from CreateTopic" in {
      val topicsDao = new TopicsDao(db)

      val topicIdFuture = topicsDao.create(sampleNewTopic)

      val topicFuture = db.run(topics.filter(_.id === sampleNewTopic.id).result.head)

      topicIdFuture.futureValue shouldBe sampleNewTopic.id
      topicFuture.futureValue shouldBe sampleNewTopic
    }

    "update a Topic record from UpdateTopic" in {
      val topicsDao = new TopicsDao(db)

      val rowsAffectedFuture = topicsDao.update(sampleTopic.id, sampleUpdatedTopic)

      val topicFuture = db.run(topics.filter(_.id === sampleUpdatedTopic.id).result.head)

      rowsAffectedFuture.futureValue shouldBe 1
      topicFuture.futureValue shouldBe sampleUpdatedTopic
      topicFuture.futureValue should !==(sampleTopic)
    }

    "delete a Topic record from DeleteTopic" in {
      val topicsDao = new TopicsDao(db)

      val rowsAffectedFuture = topicsDao.delete(sampleTopic.id)

      val topicsFuture = db.run(topics.sortBy(_.createdAt.asc).result)

      rowsAffectedFuture.futureValue shouldBe 1
      topicsFuture.futureValue.toList should not contain sampleTopic
    }
  }
}
