package com.foram.dao

import com.foram.models.Topic
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

object TopicsDao extends BaseDao {
  def findAll: Future[Seq[Topic]] = db.run(topics.result)

  def findById(id: Int): Future[Topic] = db.run(topics.filter(_.id === id).result.head)

  def findByUsername(username: String): Future[Seq[Topic]] = db.run(topics.filter(_.username === username).result)

  def findByUserID(user_id: Int): Future[Seq[Topic]] = db.run(topics.filter(_.userID === user_id).result)

  def findByCategoryID(category_id: Int): Future[Seq[Topic]] = db.run(topics.filter(_.categoryID === category_id).result)

  def create(topic: Topic): Future[Int] = db.run(topics += topic)

  def update(id: Int, topic: Topic): Future[Int] = db.run(topics.filter(_.id === topic.id).update(topic))

  def delete(id: Int): Future[Int] = db.run(topics.filter(_.id === id).delete)
}
