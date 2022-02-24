package com.foram.actors

import akka.actor.{Actor, ActorLogging}

case class Topic(id: Int, title: String, user_id: Int, category_id: Int)

object TopicDB {
  case object GetAllTopics

  case class GetTopic(id: Int)

  case class AddTopic(topic: Topic)

  case class UpdateTopic(id: Int, topic: Topic)

  case class RemoveTopic(topic: Topic)

  case object OperationSuccess
}

class TopicDB extends Actor with ActorLogging {

  import TopicDB._

  var topics: Map[Int, Topic] = Map()

  override def receive: Receive = {
    case GetAllTopics =>
      log.info(s"Searching for topics")
      sender() ! topics.values.toList

    case GetTopic(id) =>
      log.info(s"Finding topic with id: $id")
      sender() ! topics.get(id)

    case AddTopic(topic) =>
      log.info(s"Adding topic $topic")
      topics = topics + (topic.id -> topic)
      sender() ! OperationSuccess

    case UpdateTopic(id, topic) =>
      log.info(s"Updating topic $topic")
      topics = topics + (id -> topic)
      sender() ! OperationSuccess

    case RemoveTopic(topic) =>
      log.info(s"Removing topic $topic")
      topics = topics - topic.id
      sender() ! OperationSuccess
  }
}
