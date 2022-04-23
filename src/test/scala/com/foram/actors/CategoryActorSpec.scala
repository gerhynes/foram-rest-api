package com.foram.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.foram.dao.{AbstractCategoriesDao, AbstractPostsDao, AbstractTopicsDao}
import com.foram.models._
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class CategoryActorSpec extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll with MockFactory {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val sampleCategory: Category = Category(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", "javascript", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem.", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleTopic: Topic = Topic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quincy", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now())
  val samplePost: Post = Post(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleNewTopic: TopicWithChildren = TopicWithChildren(sampleTopic.id, sampleTopic.title, sampleTopic.slug, sampleTopic.user_id, sampleTopic.username, sampleTopic.category_id, sampleTopic.category_name, sampleTopic.created_at, sampleTopic.updated_at, List(samplePost))
  val sampleNewCategory: CategoryWithChildren = CategoryWithChildren(sampleCategory.id, sampleCategory.name, sampleCategory.slug, sampleCategory.user_id, sampleCategory.description, sampleCategory.created_at, sampleCategory.updated_at, List(sampleNewTopic))

  val sampleUpdatedCategory = Category(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", "javascript", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Ask questions and share tips for JavaScript and its ecosystem", OffsetDateTime.now(), OffsetDateTime.now())

  val mockCategoriesDao: AbstractCategoriesDao = stub[AbstractCategoriesDao]
  val mockTopicsDao: AbstractTopicsDao = stub[AbstractTopicsDao]
  val mockPostsDao: AbstractPostsDao = stub[AbstractPostsDao]

  val categoryActor: ActorRef = system.actorOf(Props(new CategoryActor(mockCategoriesDao, mockTopicsDao, mockPostsDao)), "categoryActor")

  implicit val timeout: Timeout = Timeout(5 seconds)

  "A CategoryActor" should {
    "respond to getAllCategories with a list of Categories" in {
      (mockCategoriesDao.findAll _).when().returns(Future(Seq(sampleCategory)))

      val categoriesFuture = categoryActor ? CategoryActor.GetAllCategories

      categoriesFuture.futureValue shouldBe List(sampleCategory)
    }

    "respond to getCategoryByID with a single Category" in {
      val uuid = UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759")
      (mockCategoriesDao.findById _).when(uuid).returns(Future(sampleCategory))

      val categoryFuture = categoryActor ? CategoryActor.GetCategoryByID(uuid)

      categoryFuture.futureValue shouldBe sampleCategory
    }

    "respond to CreateCategory with created Category" in {
      (mockCategoriesDao.create _).when(sampleCategory).returns(Future(sampleCategory.id))
      (mockTopicsDao.create _).when(sampleTopic).returns(Future(sampleTopic.id))
      (mockPostsDao.create _).when(samplePost).returns(Future(samplePost.id))

      val categoryFuture = categoryActor ? CategoryActor.CreateCategory(sampleNewCategory)

      categoryFuture.futureValue shouldBe sampleCategory
    }

    "respond to UpdateCategory with confirmation Message" in {
      (mockCategoriesDao.update _).when(sampleCategory.id, sampleUpdatedCategory).returns(Future(1))

      val categoryFuture = categoryActor ? CategoryActor.UpdateCategory(sampleCategory.id, sampleUpdatedCategory)

      categoryFuture.futureValue shouldBe Message(s"Category ${sampleUpdatedCategory.id} updated")
    }

    "respond to DeleteCategory with confirmation Message" in {
      (mockCategoriesDao.delete _).when(sampleCategory.id).returns(Future(1))

      val categoryFuture = categoryActor ? CategoryActor.DeleteCategory(sampleCategory.id)

      categoryFuture.futureValue shouldBe Message(s"Category ${sampleCategory.id} deleted")
    }
  }
}