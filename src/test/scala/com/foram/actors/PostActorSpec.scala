package com.foram.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.foram.actors.PostActor.ActionPerformed
import com.foram.dao.AbstractPostsDao
import com.foram.models.{Post, User}
import com.foram.utils.Auth
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
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

  val samplePost: Post = Post(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.parse("2022-02-20T09:30:00.155Z"), OffsetDateTime.parse("2022-03-15T12:41:13.539Z"))
  val sampleUser: User = User(UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", "quince", "qlars@example.com", Auth.hashPassword("password123"), "admin", OffsetDateTime.parse("2022-02-20T06:30:00.166Z"), OffsetDateTime.parse("2022-02-20T06:30:00.166Z"))
  val mockPostsDao: AbstractPostsDao = stub[AbstractPostsDao]
  val postActor: ActorRef = system.actorOf(Props(new PostActor(mockPostsDao)), "postActor")

  implicit val timeout: Timeout = Timeout(5 seconds)

  "A PostActor" must {
    "respond to getAllPosts with a list of Posts" in {
      (mockPostsDao.findAll _).when().returns(Future(Seq(samplePost)))

      val postsFuture = postActor ? PostActor.GetAllPosts
      postsFuture map { posts => assert(posts === List[Post](samplePost)) }
    }

    "respond to getPostByID with a single Post" in {
      val uuid = UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707")
      (mockPostsDao.findById _).when(uuid).returns(Future(samplePost))

      val postFuture = postActor ? PostActor.GetPostByID(uuid)
      postFuture map { post => assert(post === samplePost) }
    }

    "respond to getPostsByTopicID with a list of Posts" in {
      val uuid = UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946")
      (mockPostsDao.findByTopicID _).when(uuid).returns(Future(Seq(samplePost)))

      val postsFuture = postActor ? PostActor.GetPostsByTopicID(uuid)
      postsFuture map { posts => assert(posts === List[Post](samplePost)) }
    }

    "respond to getPostsByUserID with a list of Posts" in {
      val uuid = UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672")
      (mockPostsDao.findByUserID _).when(uuid).returns(Future(Seq(samplePost)))

      val postsFuture = postActor ? PostActor.GetPostsByUserID(uuid)
      postsFuture map { posts => assert(posts === List[Post](samplePost)) }
    }

    "respond to getPostsByUsername with a list of Posts" in {
      val username = "quince"
      (mockPostsDao.findByUsername _).when(username).returns(Future(Seq(samplePost)))

      val postsFuture = postActor ? PostActor.GetPostsByUsername(username)
      postsFuture map { posts => assert(posts === List[Post](samplePost)) }
    }

    "respond to CreatePost with confirmation" in {
      (mockPostsDao.create _).when(samplePost).returns(Future(samplePost.id))

      val postFuture = postActor ? PostActor.CreatePost(samplePost)
      postFuture map { success =>
        assert(success === ActionPerformed)
        expectMsg(s"Post $samplePost created.")
      }
    }

    "respond to UpdatePost with confirmation" in {
      val uuid = UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707")
      (mockPostsDao.update _).when(uuid, samplePost).returns(Future(1))

      val postFuture = postActor ? PostActor.UpdatePost(uuid, samplePost)
      postFuture map { success =>
        assert(success === ActionPerformed)
        expectMsg(s"Post $uuid updated")
      }
    }

    "respond to DeletePost with confirmation" in {
      val uuid = UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707")
      (mockPostsDao.delete _).when(uuid).returns(Future(1))

      val postFuture = postActor ? PostActor.DeletePost(uuid)
      postFuture map { success =>
        assert(success === ActionPerformed)
        expectMsg(s"Post $uuid deleted")
      }
    }
  }
}