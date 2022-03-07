package com.foram.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.foram.dao.PostsDao
import com.foram.models.Post

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object PostActor {
  case class ActionPerformed(action: String)

  case object GetAllPosts

  case class GetPostByID(id: Int)

  case class GetPostsByTopicID(topic_id: Int)

  case class GetPostsByUserID(user_id: Int)

  case class GetPostsByUsername(username: String)

  case class CreatePost(post: Post)

  case class UpdatePost(id: Int, post: Post)

  case class DeletePost(id: Int)

  def props = Props[PostActor]
}

class PostActor extends Actor with ActorLogging {

  import PostActor._

  override def receive: Receive = {
    case GetAllPosts =>
      println(s"Searching for posts")
      val postsFuture = PostsDao.findAll
      val originalSender = sender
      postsFuture.onComplete {
        case Success(posts) => originalSender ! posts.toList
        case Failure(e) =>
          println("Posts not found")
          e.printStackTrace()
      }

    case GetPostByID(id) =>
      println(s"Finding post with id: $id")
      val postFuture = PostsDao.findById(id)
      val originalSender = sender
      postFuture.onComplete {
        case Success(post) => originalSender ! post
        case Failure(e) =>
          println(s"Post $id not found")
          e.printStackTrace()
      }

    case GetPostsByTopicID(topic_id) =>
      println(s"Finding posts with topic_id: $topic_id")
      val postsFuture = PostsDao.findByTopicID(topic_id)
      val originalSender = sender
      postsFuture.onComplete {
        case Success(posts) => originalSender ! posts.toList
        case Failure(e) =>
          println(s"Posts with topic_id $topic_id not found")
          e.printStackTrace()
      }

    case GetPostsByUserID(user_id) =>
      println(s"Finding posts with user_id: $user_id")
      val postsFuture = PostsDao.findByUserID(user_id)
      val originalSender = sender
      postsFuture.onComplete {
        case Success(posts) => originalSender ! posts.toList
        case Failure(e) =>
          println(s"Posts with user_id $user_id not found")
          e.printStackTrace()
      }

    case GetPostsByUsername(username) =>
      println(s"Finding posts with username: $username")
      val postsFuture = PostsDao.findByUsername(username)
      val originalSender = sender
      postsFuture.onComplete {
        case Success(posts) => originalSender ! posts.toList
        case Failure(e) =>
          println(s"Posts with username $username not found")
          e.printStackTrace()
      }

    case CreatePost(post) =>
      println(s"Creating post $post")
      val postFuture = PostsDao.create(post)
      val originalSender = sender
      postFuture.onComplete {
        case Success(post) => originalSender ! ActionPerformed(s"Post ${post} created.")
        case Failure(e) =>
          println(s"Unable to create post $post")
          e.printStackTrace()
      }

    case UpdatePost(id, post) =>
      println(s"Updating post $id")
      val postFuture = PostsDao.update(id, post)
      val originalSender = sender
      postFuture.onComplete {
        case Success(success) => originalSender ! ActionPerformed(s"Post $id updated")
        case Failure(e) =>
          println(s"Unable to update post $id")
          e.printStackTrace()
      }

    case DeletePost(id) =>
      println(s"Removing post id $id")
      val postFuture = PostsDao.delete(id)
      val originalSender = sender
      postFuture.onComplete {
        case Success(success) => originalSender ! ActionPerformed(s"Post $id deleted")
        case Failure(e) =>
          println(s"Unable to delete post $id")
          e.printStackTrace()
      }
  }
}