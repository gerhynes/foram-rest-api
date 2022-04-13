package com.foram.routes

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.pattern.ask
import akka.testkit.TestProbe
import akka.util.Timeout
import com.foram.actors.CategoryActor.{CreateCategory, DeleteCategory, GetAllCategories, GetCategoryByID, UpdateCategory}
import com.foram.actors.{PostActor, TopicActor}
import com.foram.actors.TopicActor._
import com.foram.auth.Auth
import com.foram.models.{Category, CategoryWithChildren, Topic, TopicWithChildren, Post => MyPost}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json.DefaultJsonProtocol._
import spray.json._

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.duration._

class CategoryRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.foram.JsonFormats._

  implicit val timeout: Timeout = Timeout(5 seconds)

  val sampleCategory: Category = Category(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", "javascript", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem.", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleTopic: Topic = Topic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quince", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now())
  val samplePost: MyPost = MyPost(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleCategoryWithChildren: CategoryWithChildren =   CategoryWithChildren(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", "javascript", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem.", OffsetDateTime.now(), OffsetDateTime.now(), List(sampleTopic), List(samplePost))

  val sampleToken: String = Auth.createToken("quincy", 7)

  lazy val testKit: ActorTestKit = ActorTestKit()

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem

  implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(5.seconds)

  "CategoryRoutes" should {
    "return a list of Categories on GET requests to /api/categories" in {
      // Mock actors
      val categoryProbe: TestProbe = TestProbe("categoryProbe")
      val topicProbe: TestProbe = TestProbe("topicProbe")

      val categoryRoutes: Route = new CategoryRoutes(categoryProbe.ref, topicProbe.ref).routes

      val test = Get("/api/categories/") ~> categoryRoutes

      // Mock actor behaviour
      categoryProbe.ref ? GetAllCategories
      categoryProbe.expectMsg(3000 millis, GetAllCategories)
      categoryProbe.reply(List[Category](sampleCategory))

      test ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(List[Category](sampleCategory).toJson.toString())
      }
    }
    "return a single Category on GET requests to /api/categories/:categoryId" in {
      // Mock actors
      val categoryProbe: TestProbe = TestProbe("categoryProbe")
      val topicProbe: TestProbe = TestProbe("topicProbe")

      val categoryRoutes: Route = new CategoryRoutes(categoryProbe.ref, topicProbe.ref).routes

      val test = Get(s"/api/categories/${sampleCategory.id}") ~> categoryRoutes

      // Mock actor behaviour
      categoryProbe.ref ? GetCategoryByID(sampleCategory.id)
      categoryProbe.expectMsg(3000 millis, GetCategoryByID(sampleCategory.id))
      categoryProbe.reply(sampleCategory)

      test ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(sampleCategory.toJson.toString())
      }
    }
    "return all topics associated with a single Category on GET requests to /api/categories/:categoryId/topics" in {
      // Mock actors
      val categoryProbe: TestProbe = TestProbe("categoryProbe")
      val topicProbe: TestProbe = TestProbe("topicProbe")

      val categoryRoutes: Route = new CategoryRoutes(categoryProbe.ref, topicProbe.ref).routes

      val test = Get(s"/api/categories/${sampleCategory.id}/topics") ~> categoryRoutes

      // Mock actor behaviour
      topicProbe.ref ? TopicActor.GetTopicsByCategoryID(sampleCategory.id)
      topicProbe.expectMsg(3000 millis, TopicActor.GetTopicsByCategoryID(sampleCategory.id))
      topicProbe.reply(List[Topic](sampleTopic))

      test ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(List(sampleTopic).toJson.toString())
      }
    }
    "create a new Category on POST requests to /api/categories" in {
      // Mock actors
      val categoryProbe: TestProbe = TestProbe("categoryProbe")
      val topicProbe: TestProbe = TestProbe("topicProbe")

      val categoryRoutes: Route = new CategoryRoutes(categoryProbe.ref, topicProbe.ref).routes

      val categoryEntity = Marshal(sampleCategoryWithChildren).to[MessageEntity].futureValue

      val test = Post("/api/categories").withEntity(categoryEntity) ~> RawHeader("Authorization", s"Bearer $sampleToken") ~> categoryRoutes

      // Mock actor behaviour
      categoryProbe.ref ? CreateCategory(sampleCategoryWithChildren)
      categoryProbe.expectMsg(3000 millis, CreateCategory(sampleCategoryWithChildren))
      categoryProbe.reply(sampleCategory)

      test ~> check {
        status should ===(StatusCodes.Created)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(sampleCategory.toJson.toString())
      }
    }
    "update a Category on PUT requests to /api/categories/:categoryId" in {
      // Mock actors
      val categoryProbe: TestProbe = TestProbe("categoryProbe")
      val topicProbe: TestProbe = TestProbe("topicProbe")

      val categoryRoutes: Route = new CategoryRoutes(categoryProbe.ref, topicProbe.ref).routes

      val categoryEntity = Marshal(sampleCategory).to[MessageEntity].futureValue

      val test = Put(s"/api/categories/${sampleCategory.id}").withEntity(categoryEntity) ~> RawHeader("Authorization", s"Bearer $sampleToken") ~> categoryRoutes

      // Mock actor behaviour
      categoryProbe.ref ? UpdateCategory(sampleCategory.id, sampleCategory)
      categoryProbe.expectMsg(3000 millis, UpdateCategory(sampleCategory.id, sampleCategory))
      categoryProbe.reply(ActionPerformed(s"Category ${sampleCategory.id} updated"))

      test ~> check {
        status should ===(StatusCodes.OK)
      }
    }
    "delete a Category on DELETE requests to /api/categories/:categoryId" in {
      // Mock actors
      val categoryProbe: TestProbe = TestProbe("categoryProbe")
      val topicProbe: TestProbe = TestProbe("topicProbe")

      val categoryRoutes: Route = new CategoryRoutes(categoryProbe.ref, topicProbe.ref).routes

      val test = Delete(s"/api/categories/${sampleCategory.id}") ~> RawHeader("Authorization", s"Bearer $sampleToken") ~> categoryRoutes

      // Mock actor behaviour
      categoryProbe.ref ? DeleteCategory(sampleCategory.id)
      categoryProbe.expectMsg(3000 millis, DeleteCategory(sampleCategory.id))
      categoryProbe.reply(ActionPerformed(s"Category ${sampleCategory.id} deleted"))

      test ~> check {
        status should ===(StatusCodes.OK)
      }
    }
  }
}

