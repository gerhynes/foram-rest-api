package com.foram.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.foram.actors.UserActor.ActionPerformed
import com.foram.auth.Auth
import com.foram.dao.AbstractUsersDao
import com.foram.models.User
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.OffsetDateTime
import java.util.UUID
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

  val sampleUser: User = User(UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", "quince", "qlars@example.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.parse("2022-02-20T06:30:00.166Z"), OffsetDateTime.parse("2022-02-20T06:30:00.166Z"))
  val mockUsersDao: AbstractUsersDao = stub[AbstractUsersDao]
  val userActor: ActorRef = system.actorOf(Props(new UserActor(mockUsersDao)), "userActor")

  implicit val timeout: Timeout = Timeout(5 seconds)

  "A UserActor" should {
    "respond to getAllUsers with a list of Users" in {
      (mockUsersDao.findAll _).when().returns(Future(Seq(sampleUser)))

      val usersFuture = userActor ? UserActor.GetAllUsers
      usersFuture map { users => assert(users === List[User](sampleUser)) }
    }

    "respond to getUserByID with a single User" in {
      val uuid = UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672")
      (mockUsersDao.findById _).when(uuid).returns(Future(sampleUser))

      val userFuture = userActor ? UserActor.GetUserByID(uuid)
      userFuture map { user => assert(user === sampleUser) }
    }

    "respond to getUserByUsername with a single User" in {
      val username = "quince"
      (mockUsersDao.findByUsername _).when(username).returns(Future(sampleUser))

      val userFuture = userActor ? UserActor.GetUserByUsername(username)
      userFuture map { user => assert(user === sampleUser) }
    }

    "respond to CreateUser with confirmation" in {
      (mockUsersDao.create _).when(sampleUser).returns(Future(java.util.UUID.randomUUID))

      val userFuture = userActor ? UserActor.CreateUser(sampleUser)
      userFuture map { success =>
        assert(success === ActionPerformed)
        expectMsg(s"User $sampleUser created.")
      }
    }

    "respond to UpdateUser with confirmation" in {
      val uuid = UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672")
      (mockUsersDao.update _).when(uuid, sampleUser).returns(Future(1))

      val userFuture = userActor ? UserActor.UpdateUser(uuid, sampleUser)
      userFuture map { success =>
        assert(success === ActionPerformed)
        expectMsg(s"User $uuid updated")
      }
    }

    "respond to DeleteUser with confirmation" in {
      val uuid = UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672")
      (mockUsersDao.delete _).when(uuid).returns(Future(1))

      val userFuture = userActor ? UserActor.DeleteUser(uuid)
      userFuture map { success =>
        assert(success === ActionPerformed)
        expectMsg(s"User $uuid deleted")
      }
    }
  }
}