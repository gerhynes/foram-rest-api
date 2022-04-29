package com.foram.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.foram.daos.AbstractPostsDao
import com.foram.models.{Message, Post}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object PostActor {
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
      log.info(s"Searching for posts")
      val postsFuture = postsDao.findAll
      val originalSender = sender
      postsFuture.onComplete {
        case Success(posts) => originalSender ! posts.toList
        case Failure(ex) =>
          log.info("Posts not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case GetPostByID(id) =>
      log.info(s"Finding post with id: $id")
      val postFuture = postsDao.findById(id)
      val originalSender = sender
      postFuture.onComplete {
        case Success(post) => originalSender ! post
        case Failure(ex) =>
          log.info(s"Post $id not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case GetPostsByTopicID(topic_id) =>
      log.info(s"Finding posts with topic_id: $topic_id")
      val postsFuture = postsDao.findByTopicID(topic_id)
      val originalSender = sender
      postsFuture.onComplete {
        case Success(posts) => originalSender ! posts.toList
        case Failure(ex) =>
          log.info(s"Posts with topic_id $topic_id not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case GetPostsByUserID(user_id) =>
      log.info(s"Finding posts with user_id: $user_id")
      val postsFuture = postsDao.findByUserID(user_id)
      val originalSender = sender
      postsFuture.onComplete {
        case Success(posts) => originalSender ! posts.toList
        case Failure(ex) =>
          log.info(s"Posts with user_id $user_id not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case GetPostsByUsername(username) =>
      log.info(s"Finding posts with username: $username")
      val postsFuture = postsDao.findByUsername(username)
      val originalSender = sender
      postsFuture.onComplete {
        case Success(posts) => originalSender ! posts.toList
        case Failure(ex) =>
          log.info(s"Posts with username $username not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case CreatePost(post) =>
      log.info(s"Creating post $post")
      val postFuture = postsDao.create(post)
      val originalSender = sender
      postFuture.onComplete {
        case Success(_) => originalSender ! post
        case Failure(ex) =>
          log.info(s"Unable to create post $post")
          ex.printStackTrace()
          originalSender ! ex
      }

    case UpdatePost(id, post) =>
      log.info(s"Updating post $id")
      val postFuture = postsDao.update(id, post)
      val originalSender = sender
      postFuture.onComplete {
        case Success(_) => originalSender ! Message(s"Post $id updated")
        case Failure(ex) =>
          log.info(s"Unable to update post $id")
          ex.printStackTrace()
          originalSender ! ex
      }

    case DeletePost(id) =>
      log.info(s"Removing post id $id")
      val postFuture = postsDao.delete(id)
      val originalSender = sender
      postFuture.onComplete {
        case Success(_) => originalSender ! Message(s"Post $id deleted")
        case Failure(ex) =>
          log.info(s"Unable to delete post $id")
          ex.printStackTrace()
          originalSender ! ex
      }
  }
}