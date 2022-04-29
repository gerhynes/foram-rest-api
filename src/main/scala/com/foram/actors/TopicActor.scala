package com.foram.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.foram.daos.{AbstractPostsDao, AbstractTopicsDao}
import com.foram.models.{Message, Topic, TopicWithChildren}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object TopicActor {
  case object GetAllTopics

  case object GetLatestTopics

  case class GetTopicByID(id: UUID)

  case class GetTopicsByCategoryID(category_id: UUID)

  case class GetTopicsByUserID(user_id: UUID)

  case class GetTopicsByUsername(username: String)

  case class CreateTopic(newTopic: TopicWithChildren)

  case class UpdateTopic(id: UUID, topic: Topic)

  case class DeleteTopic(id: UUID)

  def props: Props = Props[TopicActor]
}

class TopicActor(topicsDao: AbstractTopicsDao, postsDao: AbstractPostsDao) extends Actor with ActorLogging {

  import TopicActor._

  override def receive: Receive = {
    case GetAllTopics =>
      log.info("Searching for topics")
      val topicsFuture = topicsDao.findAll
      val originalSender = sender
      topicsFuture.onComplete {
        case Success(topics) => originalSender ! topics.toList
        case Failure(ex) =>
          log.info("Topics not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case GetLatestTopics =>
      log.info("Searching for latest topics")
      val topicsFuture = topicsDao.findLatest
      val originalSender = sender
      topicsFuture.onComplete {
        case Success(topics) => originalSender ! topics.toList
        case Failure(ex) =>
          log.info("Latest topics not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case GetTopicByID(id) =>
      log.info(s"Finding topic with id: $id")
      val topicFuture = topicsDao.findById(id)
      val originalSender = sender
      topicFuture.onComplete {
        case Success(topic) => originalSender ! topic
        case Failure(ex) =>
          log.info(s"Topic $id not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case GetTopicsByCategoryID(category_id) =>
      log.info(s"Finding topics with category_id: $category_id")
      val topicsFuture = topicsDao.findByCategoryID(category_id)
      val originalSender = sender
      topicsFuture.onComplete {
        case Success(topics) => originalSender ! topics.toList
        case Failure(ex) =>
          log.info(s"Topics with category_id $category_id not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case GetTopicsByUserID(user_id) =>
      log.info(s"Finding topics with user_id: $user_id")
      val topicsFuture = topicsDao.findByUserID(user_id)
      val originalSender = sender
      topicsFuture.onComplete {
        case Success(topics) => originalSender ! topics.toList
        case Failure(ex) =>
          log.info(s"Topics with user_id $user_id not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case GetTopicsByUsername(username) =>
      log.info(s"Finding topics with username: $username")
      val topicsFuture = topicsDao.findByUsername(username)
      val originalSender = sender
      topicsFuture.onComplete {
        case Success(topics) => originalSender ! topics.toList
        case Failure(ex) =>
          log.info(s"Topics with username $username not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case CreateTopic(newTopic) =>
      log.info(s"Creating new topic $newTopic")

      // Separate topic and post
      val topic = newTopic match {
        case TopicWithChildren(id, title, slug, user_id, username, category_id, category_name, created_at, updated_at, posts) => Topic(id, title, slug, user_id, username, category_id, category_name, created_at, updated_at)
      }
      val post = newTopic.posts.head

      // Store original sender for use when future resolved
      val originalSender = sender

      // Save topic and post to database
      // Chain futures to prevent foreign key constraint violation
      val newTopicFuture = topicsDao.create(topic).flatMap(_ => postsDao.create(post))

      newTopicFuture.onComplete {
        case Success(_) => originalSender ! topic
        case Failure(ex) =>
          log.info(s"Unable to create topic $topic")
          ex.printStackTrace()
          originalSender ! ex
      }

    case UpdateTopic(id, topic) =>
      log.info(s"Updating topic $id to $topic")
      val updateFuture = topicsDao.update(id, topic)
      val originalSender = sender
      updateFuture.onComplete {
        case Success(_) => originalSender ! Message(s"Topic $id updated")
        case Failure(ex) =>
          log.info(s"Unable to update topic $id")
          ex.printStackTrace()
          originalSender ! ex
      }

    case DeleteTopic(id) =>
      log.info(s"Removing topic id $id")
      val topicFuture = topicsDao.delete(id)
      val originalSender = sender
      topicFuture.onComplete {
        case Success(_) => originalSender ! Message(s"Topic $id deleted")
        case Failure(ex) =>
          log.info(s"Unable to delete topic $id")
          ex.printStackTrace()
          originalSender ! ex
      }
  }
}