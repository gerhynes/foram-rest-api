package com.foram.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.foram.dao.TopicsDao
import com.foram.dao.PostsDao
import com.foram.models.{Post, Topic, TopicWithPosts}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object TopicActor {
  case class ActionPerformed(action: String)

  case object GetAllTopics

  case class GetTopicByID(id: UUID)

  case class GetTopicsByCategoryID(category_id: UUID)

  case class GetTopicsByUserID(user_id: UUID)

  case class GetTopicsByUsername(username: String)

  case class CreateTopic(topicWithPosts: TopicWithPosts)

  case class UpdateTopic(id: UUID, topic: Topic)

  case class DeleteTopic(id: UUID)

  def props = Props[TopicActor]
}

class TopicActor extends Actor with ActorLogging {

  import TopicActor._

  override def receive: Receive = {
    case GetAllTopics =>
      println(s"Searching for topics")
      val topicsFuture = TopicsDao.findAll
      val originalSender = sender
      topicsFuture.onComplete {
        case Success(topics) => originalSender ! topics.toList
        case Failure(e) =>
          println("Topics not found")
          e.printStackTrace()
          originalSender ! e
      }

    case GetTopicByID(id) =>
      println(s"Finding topic with id: $id")
      val topicFuture = TopicsDao.findById(id)
      val originalSender = sender
      topicFuture.onComplete {
        case Success(topic) => originalSender ! topic
        case Failure(e) =>
          println(s"Topic $id not found")
          e.printStackTrace()
          originalSender ! e
      }

    case GetTopicsByCategoryID(category_id) =>
      println(s"Finding topics with category_id: $category_id")
      val topicsFuture = TopicsDao.findByCategoryID(category_id)
      val originalSender = sender
      topicsFuture.onComplete {
        case Success(topics) => originalSender ! topics.toList
        case Failure(e) =>
          println(s"Topics with category_id $category_id not found")
          e.printStackTrace()
          originalSender ! e
      }

    case GetTopicsByUserID(user_id) =>
      println(s"Finding topics with user_id: $user_id")
      val topicsFuture = TopicsDao.findByUserID(user_id)
      val originalSender = sender
      topicsFuture.onComplete {
        case Success(topics) => originalSender ! topics.toList
        case Failure(e) =>
          println(s"Topics with user_id $user_id not found")
          e.printStackTrace()
          originalSender ! e
      }

    case GetTopicsByUsername(username) =>
      println(s"Finding topics with username: $username")
      val topicsFuture = TopicsDao.findByUsername(username)
      val originalSender = sender
      topicsFuture.onComplete {
        case Success(topics) => originalSender ! topics.toList
        case Failure(e) =>
          println(s"Topics with username $username not found")
          e.printStackTrace()
          originalSender ! e
      }

    case CreateTopic(topicWithPosts) =>
      println(s"Creating topic and post from $topicWithPosts")

      // Separate topic and post
      val topic = topicWithPosts match {
        case TopicWithPosts(id, title, slug, user_id, username, category_id, category_name, created_at, updated_at, posts) => Topic(id, title, slug, user_id, username, category_id, category_name, created_at, updated_at)
      }
      val post = topicWithPosts.posts.head

      // Store original sender for use when future resolved
      val originalSender = sender

      // Save topic and post to database
      val topicFuture = TopicsDao.create(topic)
      val postFuture = PostsDao.create(post)

      // Get results of both futures
      val result = for {
        topic_id <- topicFuture
        post_id <- postFuture
      } yield (topic_id, post_id)

      result.onComplete {
        case Success(result) => originalSender ! ActionPerformed(s"Topic $result._1 and post $result._2 created.")
        case Failure(e) =>
          println(s"Unable to create topic $topic")
          e.printStackTrace()
          originalSender ! e
      }

    case UpdateTopic(id, topic) =>
      println(s"Updating topic $id to $topic")
      val updateFuture = TopicsDao.update(id, topic)
      val originalSender = sender
      updateFuture.onComplete {
        case Success(success) => originalSender ! ActionPerformed(s"Topic $id updated")
        case Failure(e) =>
          println(s"Unable to update topic $id")
          e.printStackTrace()
          originalSender ! e
      }

    case DeleteTopic(id) =>
      println(s"Removing topic id $id")
      val topicFuture = TopicsDao.delete(id)
      val originalSender = sender
      topicFuture.onComplete {
        case Success(success) => originalSender ! ActionPerformed(s"Topic $id deleted")
        case Failure(e) =>
          println(s"Unable to delete topic $id")
          e.printStackTrace()
          originalSender ! e
      }
  }
}