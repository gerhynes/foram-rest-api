package com.foram.routes

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.testkit.TestProbe
import akka.util.Timeout
import com.foram.actors.UserActor._
import com.foram.auth.Auth
import com.foram.models.{LoginRequest, RegisteredUser, User}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class AuthRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.foram.JsonFormats._

  implicit val timeout: Timeout = Timeout(5 seconds)

  val sampleUser: User = User(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "Quincy Lars", "quincy", "qlars@example.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleToken: String = Auth.createToken(sampleUser.username, 7)
  val sampleRegisteredUser: RegisteredUser = RegisteredUser(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "Quincy Lars", "quincy", "qlars@example.com", "admin", sampleUser.created_at, sampleUser.updated_at, sampleToken)
  val sampleLoginRequest = LoginRequest(sampleUser.username, "password123")

  lazy val testKit: ActorTestKit = ActorTestKit()

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem

  implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(5.seconds)

  "AuthRoutes" should {
    "return a RegisteredUser on a successful GET request to /api/login" in {
      // Mock actors
      val userProbe = TestProbe()

      val loginEntity = Marshal(sampleLoginRequest).to[MessageEntity].futureValue

      val authRoutes = new AuthRoutes(userProbe.ref).routes

      val test = Post("/api/login").withEntity(loginEntity) ~> authRoutes

      userProbe.ref ? GetUserByUsername(sampleUser.username)
      userProbe.expectMsg(3000 millis, GetUserByUsername(sampleUser.username))
      userProbe.reply(sampleUser)

      test ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        // Cannot directly compare JSON because token generated for RegisteredUser will be unique
        Unmarshal(response).to[RegisteredUser].onComplete {
          case Success(registeredUser) => registeredUser.getClass should ===(sampleRegisteredUser.getClass)
          case Failure(ex) => ex.printStackTrace()
        }
      }
    }
  }
}

