package com.foram.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.foram.daos.{AbstractPostsDao, AbstractTopicsDao}
import com.foram.models.{Message, Post, Topic, TopicWithChildren}
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
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

  val sampleTopic: Topic = Topic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quincy", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now())
  val samplePost: Post = Post(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quincy", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleNewTopic: TopicWithChildren = TopicWithChildren(sampleTopic.id, sampleTopic.title, sampleTopic.slug, sampleTopic.user_id, sampleTopic.username, sampleTopic.category_id, sampleTopic.category_name, sampleTopic.created_at, sampleTopic.updated_at, List(samplePost))
  val mockTopicsDao: AbstractTopicsDao = stub[AbstractTopicsDao]
  val mockPostsDao: AbstractPostsDao = stub[AbstractPostsDao]
  val topicActor: ActorRef = system.actorOf(Props(new TopicActor(mockTopicsDao, mockPostsDao)), "topicActor")

  implicit val timeout: Timeout = Timeout(5 seconds)

  "A TopicActor" should {
    "respond to getAllTopics with a list of Topics" in {
      (mockTopicsDao.findAll _).when().returns(Future(Seq(sampleTopic)))

      val topicsFuture = topicActor ? TopicActor.GetAllTopics

      topicsFuture.futureValue shouldBe List(sampleTopic)
    }

    "respond to getLatestTopics with a list of Topics" in {
      (mockTopicsDao.findLatest _).when().returns(Future(Seq(sampleTopic)))

      val topicsFuture = topicActor ? TopicActor.GetLatestTopics

      topicsFuture.futureValue shouldBe List(sampleTopic)
    }

    "respond to getTopicByID with a single Topic" in {
      (mockTopicsDao.findById _).when(sampleTopic.id).returns(Future(sampleTopic))

      val topicFuture = topicActor ? TopicActor.GetTopicByID(sampleTopic.id)

      topicFuture.futureValue shouldBe sampleTopic
    }

    "respond to getTopicsByCategoryID with a list of Topics" in {
      (mockTopicsDao.findByCategoryID _).when(sampleTopic.category_id).returns(Future(Seq(sampleTopic)))

      val topicsFuture = topicActor ? TopicActor.GetTopicsByCategoryID(sampleTopic.category_id)

      topicsFuture.futureValue shouldBe List(sampleTopic)
    }

    "respond to getTopicsByUserID with a list of Topics" in {
      (mockTopicsDao.findByUserID _).when(sampleTopic.user_id).returns(Future(Seq(sampleTopic)))

      val topicsFuture = topicActor ? TopicActor.GetTopicsByUserID(sampleTopic.user_id)

      topicsFuture.futureValue shouldBe List(sampleTopic)
    }

    "respond to getTopicsByUsername with a list of Topics" in {
      (mockTopicsDao.findByUsername _).when(sampleTopic.username).returns(Future(Seq(sampleTopic)))

      val topicsFuture = topicActor ? TopicActor.GetTopicsByUsername(sampleTopic.username)

      topicsFuture.futureValue shouldBe List(sampleTopic)
    }

    "respond to CreateTopic with created Topic" in {
      (mockTopicsDao.create _).when(sampleTopic).returns(Future(sampleTopic.id))
      (mockPostsDao.create _).when(samplePost).returns(Future(samplePost.id))

      val topicFuture = topicActor ? TopicActor.CreateTopic(sampleNewTopic)

      topicFuture.futureValue shouldBe sampleTopic
    }

    "respond to UpdateTopic with confirmation Message" in {
      (mockTopicsDao.update _).when(sampleTopic.id, sampleTopic).returns(Future(1))

      val topicFuture = topicActor ? TopicActor.UpdateTopic(sampleTopic.id, sampleTopic)

      topicFuture.futureValue shouldBe Message(s"Topic ${sampleTopic.id} updated")
    }

    "respond to DeleteTopic with confirmation" in {
      (mockTopicsDao.delete _).when(sampleTopic.id).returns(Future(1))

      val topicFuture = topicActor ? TopicActor.DeleteTopic(sampleTopic.id)

      topicFuture.futureValue shouldBe Message(s"Topic ${sampleTopic.id} deleted")
    }
  }
}