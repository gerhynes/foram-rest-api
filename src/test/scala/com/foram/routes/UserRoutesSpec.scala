package com.foram.routes

import akka.actor.{ActorSystem, typed}
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.foram.models.User
import akka.testkit.TestProbe
import akka.pattern.ask

import scala.concurrent.duration._
import com.foram.actors.UserActor._
import com.foram.auth.Auth
import spray.json._

import java.time.OffsetDateTime
import java.util.UUID.randomUUID
import scala.concurrent.Future

class UserRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.foram.JsonFormats._

  implicit val timeout: Timeout = Timeout(5 seconds)

  val sampleUser: User = User(randomUUID(), "Quincy Lars", "quincy", "qlars@example.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.now(), OffsetDateTime.now())

  lazy val testKit: ActorTestKit = ActorTestKit()

  implicit def typedSystem: typed.ActorSystem[Nothing] = testKit.system

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5.seconds)

  val userProbe: TestProbe = TestProbe("userProbe")
  val topicProbe: TestProbe = TestProbe("topicProbe")
  val postProbe: TestProbe = TestProbe("postProbe")

  userProbe.ref ? GetAllUsers
  userProbe.expectMsg(3000 millis, GetAllUsers)
  userProbe.reply(List[User](sampleUser))

  val userRoutes: Route = new UserRoutes(userProbe.ref, topicProbe.ref, postProbe.ref).routes

  "UserRoutes" should {
    "return a list of Users for GET requests to the users path" in {
      Get("/api/users/") ~> userRoutes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)
        
        entityAs[String] should ===(List[User](sampleUser).toString())
        //        responseAs[List[User]] shouldEqual ???
      }
    }
  }
}

