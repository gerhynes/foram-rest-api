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
import com.foram.actors.PostActor.GetPostsByUsername
import com.foram.actors.TopicActor.GetTopicsByUsername
import com.foram.actors.UserActor._
import com.foram.auth.Auth
import com.foram.models.{Message, RegisteredUser, Topic, User, Post => MyPost}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json.DefaultJsonProtocol._
import spray.json._

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.duration._

class UserRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.foram.JsonFormats._

  implicit val timeout: Timeout = Timeout(5 seconds)

  val sampleUser: User = User(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "Quincy Lars", "quincy", "qlars@example.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleToken: String = Auth.createToken(sampleUser.username, 7)
  val sampleTopic: Topic = Topic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quince", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now())
  // Post aliased to MyPost to prevent name collision with RequestBuilder
  val samplePost: MyPost = MyPost(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleRegisteredUser: RegisteredUser = RegisteredUser(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "Quincy Lars", "quincy", "qlars@example.com", "admin", OffsetDateTime.now(), OffsetDateTime.now(),
    sampleToken)

  lazy val testKit: ActorTestKit = ActorTestKit()

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem

  implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(5.seconds)

  "UserRoutes" should {
    "return a list of Users for GET requests to /api/users" in {
      // Mock actors
      val userProbe = TestProbe()
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val userRoutes: Route = new UserRoutes(userProbe.ref, topicProbe.ref, postProbe.ref).routes

      val test = Get("/api/users/") ~> userRoutes

      userProbe.ref ? GetAllUsers
      userProbe.expectMsg(3000 millis, GetAllUsers)
      userProbe.reply(List[User](sampleUser))

      test ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(List[User](sampleUser).toJson.toString())
      }
    }
    "return a single User for GET requests to /api/users/:username" in {
      // Mock actors
      val userProbe = TestProbe()
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val userRoutes: Route = new UserRoutes(userProbe.ref, topicProbe.ref, postProbe.ref).routes

      val test = Get(s"/api/users/${sampleUser.username}") ~> userRoutes

      userProbe.ref ? GetUserByUsername(sampleUser.username)
      userProbe.expectMsg(3000 millis, GetUserByUsername(sampleUser.username))
      userProbe.reply(sampleUser)

      test ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(sampleUser.toJson.toString())
      }
    }
    "return all topics associated with a single User for GET requests to /api/users/:username/topics" in {
      // Mock actors
      val userProbe = TestProbe("userProbe")
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val userRoutes: Route = new UserRoutes(userProbe.ref, topicProbe.ref, postProbe.ref).routes

      val test = Get(s"/api/users/${sampleUser.username}/topics") ~> userRoutes

      topicProbe.ref ? GetTopicsByUsername(sampleUser.username)
      topicProbe.expectMsg(3000 millis, GetTopicsByUsername(sampleUser.username))
      topicProbe.reply(List[Topic](sampleTopic))

      test ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(List(sampleTopic).toJson.toString())
      }
    }
    "return all posts associated with a single User for GET requests to /api/users/:username/posts" in {
      // Mock actors
      val userProbe = TestProbe("userProbe")
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val userRoutes: Route = new UserRoutes(userProbe.ref, topicProbe.ref, postProbe.ref).routes

      val test = Get(s"/api/users/${sampleUser.username}/posts") ~> userRoutes

      postProbe.ref ? GetPostsByUsername(sampleUser.username)
      postProbe.expectMsg(3000 millis, GetPostsByUsername(sampleUser.username))
      postProbe.reply(List[MyPost](samplePost))

      test ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(List(samplePost).toJson.toString())
      }
    }
    "create a new User for POST requests to /api/users" in {
      // Mock actors
      val userProbe = TestProbe("userProbe")
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val userRoutes: Route = new UserRoutes(userProbe.ref, topicProbe.ref, postProbe.ref).routes

      val userEntity = Marshal(sampleUser).to[MessageEntity].futureValue

      val test = Post("/api/users").withEntity(userEntity) ~> userRoutes

      userProbe.ref ? CreateUser(sampleUser)
      userProbe.expectMsg(3000 millis, CreateUser(sampleUser))
      userProbe.reply(sampleRegisteredUser)

      test ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(sampleRegisteredUser.toJson.toString())
      }
    }
    "update a User on PUT requests to /api/users/:userId" in {
      // Mock actors
      val userProbe = TestProbe("userProbe")
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val userRoutes: Route = new UserRoutes(userProbe.ref, topicProbe.ref, postProbe.ref).routes

      val userEntity = Marshal(sampleUser).to[MessageEntity].futureValue

      val test = Put(s"/api/users/${sampleUser.id}").withEntity(userEntity) ~> RawHeader("Authorization", s"Bearer $sampleToken") ~> userRoutes

      userProbe.ref ? UpdateUser(sampleUser.id, sampleUser)
      userProbe.expectMsg(3000 millis, UpdateUser(sampleUser.id, sampleUser))
      userProbe.reply(Message(s"User ${sampleUser.id} updated"))

      test ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(Message(s"User ${sampleUser.id} updated").toJson.toString())
      }
    }
    "delete a User on DELETE requests to /api/users/:userId" in {
      // Mock actors
      val userProbe = TestProbe("userProbe")
      val topicProbe: TestProbe = TestProbe("topicProbe")
      val postProbe: TestProbe = TestProbe("postProbe")

      val userRoutes: Route = new UserRoutes(userProbe.ref, topicProbe.ref, postProbe.ref).routes

      val test = Delete(s"/api/users/${sampleUser.id}") ~> RawHeader("Authorization", s"Bearer $sampleToken") ~> userRoutes

      userProbe.ref ? DeleteUser(sampleUser.id)
      userProbe.expectMsg(3000 millis, DeleteUser(sampleUser.id))
      userProbe.reply(Message(s"User ${sampleUser.id} deleted"))

      test ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(Message(s"User ${sampleUser.id} deleted").toJson.toString())
      }
    }
  }
}

