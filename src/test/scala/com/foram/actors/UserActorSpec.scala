package com.foram.actors

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout
import com.foram.actors.UserActor.GetAllUsers
import com.foram.dao.UsersDao
import com.foram.models.User
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

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

  val usersDao: UsersDao = mock[UsersDao]

  implicit val timeout = Timeout(5 seconds)

  "A UserActor" must {
    "respond to getUsers with users" in {
      val userActor = system.actorOf(Props[UserActor])
      userActor ? UserActor.GetAllUsers
      expectMsgType[List[User]]
    }
  }
}
