package com.foram.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.foram.actors.PostActor.ActionPerformed
import com.foram.dao.AbstractCategoriesDao
import com.foram.models._
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
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

  val sampleCategory: Category = Category(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", "javascript", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem.", OffsetDateTime.parse("2022-02-20T07:30:00.155Z"), OffsetDateTime.parse("2022-02-20T07:30:00.155Z"))
  val sampleTopic: Topic = Topic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quince", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.parse("2022-02-20T09:30:00.155Z"), OffsetDateTime.parse("2022-02-20T09:30:00.155Z"))
  val samplePost: Post = Post(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.parse("2022-02-20T09:30:00.155Z"), OffsetDateTime.parse("2022-03-15T12:41:13.539Z"))
  val sampleNewTopic: NewTopic = NewTopic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quince", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.parse("2022-02-20T09:30:00.155Z"), OffsetDateTime.parse("2022-02-20T09:30:00.155Z"), List(samplePost))
  val sampleNewCategory: NewCategory = NewCategory(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", "javascript", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem.", OffsetDateTime.parse("2022-02-20T07:30:00.155Z"), OffsetDateTime.parse("2022-02-20T07:30:00.155Z"), List(sampleTopic), List(samplePost))

  val mockCategoriesDao: AbstractCategoriesDao = stub[AbstractCategoriesDao]
  val categoryActor: ActorRef = system.actorOf(Props(new CategoryActor(mockCategoriesDao)), "categoryActor")

  implicit val timeout: Timeout = Timeout(5 seconds)

  "A CategoryActor" must {
    "respond to getAllCategories with a list of Categories" in {
      (mockCategoriesDao.findAll _).when().returns(Future(Seq(sampleCategory)))

      val categoriesFuture = categoryActor ? CategoryActor.GetAllCategories
      categoriesFuture map { categories => assert(categories === List[Category](sampleCategory)) }
    }

    "respond to getCategoryByID with a single Category" in {
      val uuid = UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759")
      (mockCategoriesDao.findById _).when(uuid).returns(Future(sampleCategory))

      val categoryFuture = categoryActor ? CategoryActor.GetCategoryByID(uuid)
      categoryFuture map { category => assert(category === sampleCategory) }
    }

    "respond to CreateCategory with confirmation" in {
      (mockCategoriesDao.create _).when(sampleCategory).returns(Future(sampleCategory.id))

      val categoryFuture = categoryActor ? CategoryActor.CreateCategory(sampleNewCategory)
      categoryFuture map { success =>
        assert(success === ActionPerformed)
        expectMsg("Category created")
      }
    }

    "respond to UpdateCategory with confirmation" in {
      val uuid = UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759")
      (mockCategoriesDao.update _).when(uuid, sampleCategory).returns(Future(1))

      val categoryFuture = categoryActor ? CategoryActor.UpdateCategory(uuid, sampleCategory)
      categoryFuture map { success =>
        assert(success === ActionPerformed)
        expectMsg("Category updated")
      }
    }

    "respond to DeleteCategory with confirmation" in {
      val uuid = UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759")
      (mockCategoriesDao.delete _).when(uuid).returns(Future(1))

      val categoryFuture = categoryActor ? CategoryActor.DeleteCategory(uuid)
      categoryFuture map { success =>
        assert(success === ActionPerformed)
        expectMsg("Category deleted")
      }
    }
  }
}