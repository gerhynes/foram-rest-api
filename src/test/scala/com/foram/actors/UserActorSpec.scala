package com.foram.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.foram.auth.Auth
import com.foram.dao.AbstractUsersDao
import com.foram.models.{Message, RegisteredUser, User}
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.OffsetDateTime
import java.util.UUID.randomUUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._


class UserActorSpec extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll with MockFactory {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val sampleUser: User = User(randomUUID(), "Quincy Lars", "quincy", "qlars@example.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.now(), OffsetDateTime.now())
  val mockUsersDao: AbstractUsersDao = stub[AbstractUsersDao]
  val userActor: ActorRef = system.actorOf(Props(new UserActor(mockUsersDao)), "userActor")

  implicit val timeout: Timeout = Timeout(5 seconds)

  "A UserActor" should {
    "respond to getAllUsers with a list of Users" in {
      (mockUsersDao.findAll _).when().returns(Future(Seq(sampleUser)))

      val usersFuture = userActor ? UserActor.GetAllUsers

      usersFuture.futureValue shouldBe List(sampleUser)
    }

    "respond to getUserByID with a single User" in {
      (mockUsersDao.findById _).when(sampleUser.id).returns(Future(sampleUser))

      val userFuture = userActor ? UserActor.GetUserByID(sampleUser.id)

      userFuture.futureValue shouldBe sampleUser
    }

    "respond to getUserByUsername with a single User" in {
      (mockUsersDao.findByUsername _).when(sampleUser.username).returns(Future(sampleUser))

      val userFuture = userActor ? UserActor.GetUserByUsername(sampleUser.username)

      userFuture.futureValue shouldBe sampleUser
    }

    "respond to CreateUser with a RegisteredUser" in {
      (mockUsersDao.create _).when(sampleUser).returns(Future(sampleUser.id))

      val userFuture = userActor ? UserActor.CreateUser(sampleUser)

      userFuture.futureValue shouldBe a[RegisteredUser]
    }

    "respond to UpdateUser with confirmation Message" in {
      (mockUsersDao.update _).when(sampleUser.id, sampleUser).returns(Future(1))

      val userFuture = userActor ? UserActor.UpdateUser(sampleUser.id, sampleUser)

      userFuture.futureValue shouldBe Message(s"User ${sampleUser.id.toString} updated")
    }

    "respond to DeleteUser with confirmation Message" in {
      (mockUsersDao.delete _).when(sampleUser.id).returns(Future(1))

      val userFuture = userActor ? UserActor.DeleteUser(sampleUser.id)

      userFuture.futureValue shouldBe Message(s"User ${sampleUser.id.toString} deleted")
    }
  }
}