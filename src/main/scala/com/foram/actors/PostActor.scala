package com.foram.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.foram.dao.AbstractPostsDao
import com.foram.models.{Message, Post}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object PostActor {
  case class ActionPerformed(action: String)

  case object GetAllPosts

  case class GetPostByID(id: UUID)

  case class GetPostsByTopicID(topic_id: UUID)

  case class GetPostsByUserID(user_id: UUID)

  case class GetPostsByUsername(username: String)

  case class CreatePost(post: Post)

  case class UpdatePost(id: UUID, post: Post)

  case class DeletePost(id: UUID)

  def props = Props[PostActor]
}

class PostActor(postsDao: AbstractPostsDao) extends Actor with ActorLogging {

  import PostActor._

  override def receive: Receive = {
    case GetAllPosts =>
      println(s"Searching for posts")
      val postsFuture = postsDao.findAll
      val originalSender = sender
      postsFuture.onComplete {
        case Success(posts) => originalSender ! posts.toList
        case Failure(e) =>
          println("Posts not found")
          e.printStackTrace()
          originalSender ! e
      }

    case GetPostByID(id) =>
      println(s"Finding post with id: $id")
      val postFuture = postsDao.findById(id)
      val originalSender = sender
      postFuture.onComplete {
        case Success(post) => originalSender ! post
        case Failure(e) =>
          println(s"Post $id not found")
          e.printStackTrace()
          originalSender ! e
      }

    case GetPostsByTopicID(topic_id) =>
      println(s"Finding posts with topic_id: $topic_id")
      val postsFuture = postsDao.findByTopicID(topic_id)
      val originalSender = sender
      postsFuture.onComplete {
        case Success(posts) => originalSender ! posts.toList
        case Failure(e) =>
          println(s"Posts with topic_id $topic_id not found")
          e.printStackTrace()
          originalSender ! e
      }

    case GetPostsByUserID(user_id) =>
      println(s"Finding posts with user_id: $user_id")
      val postsFuture = postsDao.findByUserID(user_id)
      val originalSender = sender
      postsFuture.onComplete {
        case Success(posts) => originalSender ! posts.toList
        case Failure(e) =>
          println(s"Posts with user_id $user_id not found")
          e.printStackTrace()
          originalSender ! e
      }

    case GetPostsByUsername(username) =>
      println(s"Finding posts with username: $username")
      val postsFuture = postsDao.findByUsername(username)
      val originalSender = sender
      postsFuture.onComplete {
        case Success(posts) => originalSender ! posts.toList
        case Failure(e) =>
          println(s"Posts with username $username not found")
          e.printStackTrace()
          originalSender ! e
      }

    case CreatePost(post) =>
      println(s"Creating post $post")
      val postFuture = postsDao.create(post)
      val originalSender = sender
      postFuture.onComplete {
        case Success(postId) => originalSender ! post
        case Failure(e) =>
          println(s"Unable to create post $post")
          e.printStackTrace()
          originalSender ! e
      }

    case UpdatePost(id, post) =>
      println(s"Updating post $id")
      val postFuture = postsDao.update(id, post)
      val originalSender = sender
      postFuture.onComplete {
        case Success(success) => originalSender ! Message(s"Post $id updated")
        case Failure(e) =>
          println(s"Unable to update post $id")
          e.printStackTrace()
          originalSender ! e
      }

    case DeletePost(id) =>
      println(s"Removing post id $id")
      val postFuture = postsDao.delete(id)
      val originalSender = sender
      postFuture.onComplete {
        case Success(success) => originalSender ! Message(s"Post $id deleted")
        case Failure(e) =>
          println(s"Unable to delete post $id")
          e.printStackTrace()
          originalSender ! e
      }
  }
}