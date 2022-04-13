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
import com.foram.actors.PostActor._
import com.foram.auth.Auth
import com.foram.models.{Post => MyPost}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json.DefaultJsonProtocol._
import spray.json._

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.duration._

class PostRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.foram.JsonFormats._

  implicit val timeout: Timeout = Timeout(5 seconds)

  val samplePost: MyPost = MyPost(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleToken: String = Auth.createToken("quincy", 7)

  lazy val testKit: ActorTestKit = ActorTestKit()

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem

  implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(5.seconds)

  "PostRoutes" should {
    "return a list of Posts on GET requests to /api/posts" in {
      // Mock actors
      val postProbe: TestProbe = TestProbe("postProbe")

      val postRoutes: Route = new PostRoutes(postProbe.ref).routes

      val test = Get("/api/posts/") ~> postRoutes

      postProbe.ref ? GetAllPosts
      postProbe.expectMsg(3000 millis, GetAllPosts)
      postProbe.reply(List[MyPost](samplePost))

      test ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(List[MyPost](samplePost).toJson.toString())
      }
    }
    "return a single Post on GET requests to /api/posts/:postId" in {
      // Mock actors
      val postProbe: TestProbe = TestProbe("postProbe")

      val postRoutes: Route = new PostRoutes(postProbe.ref).routes

      val test = Get(s"/api/posts/${samplePost.id}") ~> postRoutes

      postProbe.ref ? GetPostByID(samplePost.id)
      postProbe.expectMsg(3000 millis, GetPostByID(samplePost.id))
      postProbe.reply(samplePost)

      test ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(samplePost.toJson.toString())
      }
    }
    "create a new Post on POST requests to /api/posts" in {
      // Mock actors
      val postProbe: TestProbe = TestProbe("postProbe")

      val postRoutes: Route = new PostRoutes(postProbe.ref).routes

      val postEntity = Marshal(samplePost).to[MessageEntity].futureValue

      val test = Post("/api/posts").withEntity(postEntity) ~> RawHeader("Authorization", s"Bearer $sampleToken") ~> postRoutes

      postProbe.ref ? CreatePost(samplePost)
      postProbe.expectMsg(3000 millis, CreatePost(samplePost))
      postProbe.reply(samplePost)

      test ~> check {
        status should ===(StatusCodes.Created)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(samplePost.toJson.toString())
      }
    }
    "update a Post on PUT requests to /api/posts/:postId" in {
      // Mock actors
      val postProbe: TestProbe = TestProbe("postProbe")

      val postRoutes: Route = new PostRoutes(postProbe.ref).routes

      val postEntity = Marshal(samplePost).to[MessageEntity].futureValue

      val test = Put(s"/api/posts/${samplePost.id}").withEntity(postEntity) ~> RawHeader("Authorization", s"Bearer $sampleToken") ~> postRoutes

      postProbe.ref ? UpdatePost(samplePost.id, samplePost)
      postProbe.expectMsg(3000 millis, UpdatePost(samplePost.id, samplePost))
      postProbe.reply(ActionPerformed(s"User $samplePost.id updated"))

      test ~> check {
        status should ===(StatusCodes.OK)
      }
    }
    "delete a Post on DELETE requests to /api/posts/:postId" in {
      // Mock actors
      val postProbe: TestProbe = TestProbe("postProbe")

      val postRoutes: Route = new PostRoutes(postProbe.ref).routes

      val test = Delete(s"/api/posts/${samplePost.id}") ~> RawHeader("Authorization", s"Bearer $sampleToken") ~> postRoutes

      postProbe.ref ? DeletePost(samplePost.id)
      postProbe.expectMsg(3000 millis, DeletePost(samplePost.id))
      postProbe.reply(ActionPerformed(s"User $samplePost.id deleted"))

      test ~> check {
        status should ===(StatusCodes.OK)
      }
    }
  }
}

