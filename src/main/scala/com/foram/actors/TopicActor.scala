package com.foram.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.foram.dao.TopicsDao
import com.foram.models.Topic

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object TopicActor {
  case class ActionPerformed(action: String)

  case object GetAllTopics

  case class GetTopicByID(id: Int)

  case class GetTopicsByCategoryID(category_id: Int)

  case class GetTopicsByUserID(user_id: Int)

  case class GetTopicsByUsername(username: String)

  case class CreateTopic(topic: Topic)

  case class UpdateTopic(id: Int, topic: Topic)

  case class DeleteTopic(id: Int)

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
      }

    case CreateTopic(topic) =>
      println(s"Creating topic $topic")
      val topicFuture = TopicsDao.create(topic)
      val originalSender = sender
      topicFuture.onComplete {
        case Success(success) => originalSender ! ActionPerformed(s"Topic ${topic.title} created.")
        case Failure(e) =>
          println(s"Unable to create topic $topic")
          e.printStackTrace()
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
      }
  }
}