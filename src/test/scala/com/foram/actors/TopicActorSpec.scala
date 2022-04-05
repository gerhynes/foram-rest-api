package com.foram.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.foram.actors.PostActor.ActionPerformed
import com.foram.dao.AbstractTopicsDao
import com.foram.models.{NewTopic, Post, Topic}
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class TopicActorSpec extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll with MockFactory {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val sampleTopic: Topic = Topic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quince", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.parse("2022-02-20T09:30:00.155Z"), OffsetDateTime.parse("2022-02-20T09:30:00.155Z"))
  val samplePost: Post = Post(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.parse("2022-02-20T09:30:00.155Z"), OffsetDateTime.parse("2022-03-15T12:41:13.539Z"))
  val sampleNewTopic: NewTopic = NewTopic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quince", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.parse("2022-02-20T09:30:00.155Z"), OffsetDateTime.parse("2022-02-20T09:30:00.155Z"), List(samplePost))
  val mockTopicsDao: AbstractTopicsDao = stub[AbstractTopicsDao]
  val topicActor: ActorRef = system.actorOf(Props(new TopicActor(mockTopicsDao)), "topicActor")

  implicit val timeout: Timeout = Timeout(5 seconds)

  "A TopicActor" must {
    "respond to getAllTopics with a list of Topics" in {
      (mockTopicsDao.findAll _).when().returns(Future(Seq(sampleTopic)))

      val topicsFuture = topicActor ? TopicActor.GetAllTopics
      topicsFuture map { topics => assert(topics === List[Topic](sampleTopic)) }
    }

    "respond to getLatestTopics with a list of Topics" in {
      (mockTopicsDao.findAll _).when().returns(Future(Seq(sampleTopic)))

      val topicsFuture = topicActor ? TopicActor.GetLatestTopics
      topicsFuture map { topics => assert(topics === List[Topic](sampleTopic)) }
    }

    "respond to getTopicByID with a single Topic" in {
      val uuid = UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946")
      (mockTopicsDao.findById _).when(uuid).returns(Future(sampleTopic))

      val topicFuture = topicActor ? TopicActor.GetTopicByID(uuid)
      topicFuture map { topic => assert(topic === sampleTopic) }
    }

    "respond to getTopicsByCategoryID with a list of Topics" in {
      val uuid = UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759")
      (mockTopicsDao.findByCategoryID _).when(uuid).returns(Future(Seq(sampleTopic)))

      val topicsFuture = topicActor ? TopicActor.GetTopicsByCategoryID(uuid)
      topicsFuture map { topics => assert(topics === List[Topic](sampleTopic)) }
    }

    "respond to getTopicsByUserID with a list of Topics" in {
      val uuid = UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672")

      (mockTopicsDao.findByUserID _).when(uuid).returns(Future(Seq(sampleTopic)))

      val topicsFuture = topicActor ? TopicActor.GetTopicsByUserID
      topicsFuture map { topics => assert(topics === List[Topic](sampleTopic)) }
    }

    "respond to getTopicsByUsername with a list of Topics" in {
      val username = "quince"
      (mockTopicsDao.findByUsername _).when(username).returns(Future(Seq(sampleTopic)))

      val topicsFuture = topicActor ? TopicActor.GetTopicsByUsername(username)
      topicsFuture map { topics => assert(topics === List[Topic](sampleTopic)) }
    }

    "respond to CreateTopic with confirmation" in {
      (mockTopicsDao.create _).when(sampleTopic).returns(Future(sampleNewTopic.id))

      val topicFuture = topicActor ? TopicActor.CreateTopic(sampleNewTopic)
      topicFuture map { result =>
        assert(result === ActionPerformed)
        expectMsg(s"Topic $result._1 and post $result._2 created.")
      }
    }

    "respond to UpdateTopic with confirmation" in {
      val uuid = UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946")
      (mockTopicsDao.update _).when(uuid, sampleTopic).returns(Future(1))

      val topicFuture = topicActor ? TopicActor.UpdateTopic(uuid, sampleTopic)
      topicFuture map { success =>
        assert(success === ActionPerformed)
        expectMsg(s"Topic $uuid updated")
      }
    }

    "respond to DeleteTopic with confirmation" in {
      val uuid = UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946")
      (mockTopicsDao.delete _).when(uuid).returns(Future(1))

      val topicFuture = topicActor ? TopicActor.DeleteTopic(uuid)
      topicFuture map { success =>
        assert(success === ActionPerformed)
        expectMsg(s"Topic $uuid deleted")
      }
    }
  }
}