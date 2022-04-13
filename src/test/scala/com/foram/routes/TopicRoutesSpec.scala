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
import com.foram.actors.PostActor
import com.foram.actors.TopicActor._
import com.foram.auth.Auth
import com.foram.models.{Topic, TopicWithChildren, Post => MyPost}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json.DefaultJsonProtocol._
import spray.json._

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.duration._

class TopicRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.foram.JsonFormats._

  implicit val timeout: Timeout = Timeout(5 seconds)

  val sampleTopic: Topic = Topic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quince", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now())
  val samplePost: MyPost = MyPost(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleTopicWithChildren: TopicWithChildren = TopicWithChildren(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quince", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now(), List(samplePost))
  val sampleToken: String = Auth.createToken("quincy", 7)

  lazy val testKit: ActorTestKit = ActorTestKit()

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem

  implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(5.seconds)

  "TopicRoutes" should {
    "return a list of Topics on GET requests to /api/topics" in {
      // Mock actors
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val topicRoutes: Route = new TopicRoutes(topicProbe.ref, postProbe.ref).routes

      val test = Get("/api/topics/") ~> topicRoutes

      // Mock actor behaviour
      topicProbe.ref ? GetAllTopics
      topicProbe.expectMsg(3000 millis, GetAllTopics)
      topicProbe.reply(List[Topic](sampleTopic))

      test ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(List[Topic](sampleTopic).toJson.toString())
      }
    }
    "return a single Topic on GET requests to /api/topics/:topicId" in {
      // Mock actors
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val topicRoutes: Route = new TopicRoutes(topicProbe.ref, postProbe.ref).routes

      val test = Get(s"/api/topics/${sampleTopic.id}") ~> topicRoutes

      // Mock actor behaviour
      topicProbe.ref ? GetTopicByID(sampleTopic.id)
      topicProbe.expectMsg(3000 millis, GetTopicByID(sampleTopic.id))
      topicProbe.reply(sampleTopic)

      test ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(sampleTopic.toJson.toString())
      }
    }
    "return all posts associated with a single Topic on GET requests to /api/topics/:topicId/posts" in {
      // Mock actors
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val topicRoutes: Route = new TopicRoutes(topicProbe.ref, postProbe.ref).routes

      val test = Get(s"/api/topics/${sampleTopic.id}/posts") ~> topicRoutes

      // Mock actor behaviour
      postProbe.ref ? PostActor.GetPostsByTopicID(sampleTopic.id)
      postProbe.expectMsg(3000 millis, PostActor.GetPostsByTopicID(sampleTopic.id))
      postProbe.reply(List[MyPost](samplePost))

      test ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(List(samplePost).toJson.toString())
      }
    }
    "return latest Topics on GET requests to /api/topics/latest" in {
      // Mock actors
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val topicRoutes: Route = new TopicRoutes(topicProbe.ref, postProbe.ref).routes

      val test = Get("/api/topics/latest") ~> topicRoutes

      // Mock actor behaviour
      topicProbe.ref ? GetLatestTopics
      topicProbe.expectMsg(3000 millis, GetLatestTopics)
      topicProbe.reply(List[Topic](sampleTopic))

      test ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(List(sampleTopic).toJson.toString())
      }
    }
    "create a new Topic on POST requests to /api/topics" in {
      // Mock actors
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val topicRoutes: Route = new TopicRoutes(topicProbe.ref, postProbe.ref).routes

      val topicEntity = Marshal(sampleTopicWithChildren).to[MessageEntity].futureValue

      val test = Post("/api/topics").withEntity(topicEntity) ~> RawHeader("Authorization", s"Bearer $sampleToken") ~> topicRoutes

      // Mock actor behaviour
      topicProbe.ref ? CreateTopic(sampleTopicWithChildren)
      topicProbe.expectMsg(3000 millis, CreateTopic(sampleTopicWithChildren))
      topicProbe.reply(sampleTopic)

      test ~> check {
        status should ===(StatusCodes.Created)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(sampleTopic.toJson.toString())
      }
    }
    "update a Topic on PUT requests to /api/topics/:topicId" in {
      // Mock actors
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val topicRoutes: Route = new TopicRoutes(topicProbe.ref, postProbe.ref).routes

      val topicEntity = Marshal(sampleTopic).to[MessageEntity].futureValue

      val test = Put(s"/api/topics/${sampleTopic.id}").withEntity(topicEntity) ~> RawHeader("Authorization", s"Bearer $sampleToken") ~> topicRoutes

      // Mock actor behaviour
      topicProbe.ref ? UpdateTopic(sampleTopic.id, sampleTopic)
      topicProbe.expectMsg(3000 millis, UpdateTopic(sampleTopic.id, sampleTopic))
      topicProbe.reply(ActionPerformed(s"Topic $sampleTopic.id updated"))

      test ~> check {
        status should ===(StatusCodes.OK)
      }
    }
    "delete a Topic on DELETE requests to /api/topics/:topicId" in {
      // Mock actors
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val topicRoutes: Route = new TopicRoutes(topicProbe.ref, postProbe.ref).routes

      val test = Delete(s"/api/topics/${sampleTopic.id}") ~> RawHeader("Authorization", s"Bearer $sampleToken") ~> topicRoutes

      // Mock actor behaviour
      topicProbe.ref ? DeleteTopic(sampleTopic.id)
      topicProbe.expectMsg(3000 millis, DeleteTopic(sampleTopic.id))
      topicProbe.reply(ActionPerformed(s"Topic $sampleTopic.id deleted"))

      test ~> check {
        status should ===(StatusCodes.OK)
      }
    }
  }
}

