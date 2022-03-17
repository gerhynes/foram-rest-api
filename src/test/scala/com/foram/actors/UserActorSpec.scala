package com.foram.actors

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout
import com.foram.actors.UserActor.GetAllUsers
import com.foram.dao.AbstractUsersDao
import com.foram.models.User
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
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

  val user = User(UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", "quince", "qlars@example.com", OffsetDateTime.parse("2022-02-20T06:30:00.166Z"), OffsetDateTime.parse("2022-02-20T06:30:00.166Z"))

  implicit val timeout = Timeout(5 seconds)

  "A UserActor" must {
    "respond to getUsers with list of users" in {
      val mockUsersDao = stub[AbstractUsersDao]
      (mockUsersDao.findAll _).when().returns(Future(Seq(user)))

      val userActor = system.actorOf(Props(new UserActor(mockUsersDao)), "userActor")
      val usersFuture = userActor ? UserActor.GetAllUsers
      usersFuture map { users => assert(users === List[User](user)) }
    }
  }
}
