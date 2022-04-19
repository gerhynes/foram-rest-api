package com.foram.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.foram.auth.Auth
import com.foram.dao.AbstractPostsDao
import com.foram.models.{Message, Post, User}
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

class PostActorSpec extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll with MockFactory {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val samplePost: Post = Post(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.now(), OffsetDateTime.now())
  val sampleUser: User = User(UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", "quincy", "qlars@example.com","password123", "admin", OffsetDateTime.now(), OffsetDateTime.now())
  val mockPostsDao: AbstractPostsDao = stub[AbstractPostsDao]
  val postActor: ActorRef = system.actorOf(Props(new PostActor(mockPostsDao)), "postActor")

  implicit val timeout: Timeout = Timeout(5 seconds)

  "A PostActor" should {
    "respond to getAllPosts with a list of Posts" in {
      (mockPostsDao.findAll _).when().returns(Future(Seq(samplePost)))

      val postsFuture = postActor ? PostActor.GetAllPosts

      postsFuture.futureValue shouldBe List(samplePost)
    }

    "respond to getPostByID with a single Post" in {
      (mockPostsDao.findById _).when(samplePost.id).returns(Future(samplePost))

      val postFuture = postActor ? PostActor.GetPostByID(samplePost.id)

      postFuture.futureValue shouldBe samplePost
    }

    "respond to getPostsByTopicID with a list of Posts" in {
      (mockPostsDao.findByTopicID _).when(samplePost.topic_id).returns(Future(Seq(samplePost)))

      val postsFuture = postActor ? PostActor.GetPostsByTopicID(samplePost.topic_id)

      postsFuture.futureValue shouldBe List(samplePost)
    }

    "respond to getPostsByUserID with a list of Posts" in {
      (mockPostsDao.findByUserID _).when(sampleUser.id).returns(Future(Seq(samplePost)))

      val postsFuture = postActor ? PostActor.GetPostsByUserID(sampleUser.id)

      postsFuture.futureValue shouldBe List(samplePost)
    }

    "respond to getPostsByUsername with a list of Posts" in {
      (mockPostsDao.findByUsername _).when(sampleUser.username).returns(Future(Seq(samplePost)))

      val postsFuture = postActor ? PostActor.GetPostsByUsername(sampleUser.username)

      postsFuture.futureValue shouldBe List(samplePost)
    }

    "respond to CreatePost with Post created" in {
      (mockPostsDao.create _).when(samplePost).returns(Future(samplePost.id))

      val postFuture = postActor ? PostActor.CreatePost(samplePost)

      postFuture.futureValue shouldBe samplePost
    }

    "respond to UpdatePost with confirmation Message" in {
      (mockPostsDao.update _).when(samplePost.id, samplePost).returns(Future(1))

      val postFuture = postActor ? PostActor.UpdatePost(samplePost.id, samplePost)

      postFuture.futureValue shouldBe Message(s"Post ${samplePost.id} updated")
    }

    "respond to DeletePost with confirmation Message" in {
      (mockPostsDao.delete _).when(samplePost.id).returns(Future(1))

      val postFuture = postActor ? PostActor.DeletePost(samplePost.id)

      postFuture.futureValue shouldBe Message(s"Post ${samplePost.id} deleted")
    }
  }
}