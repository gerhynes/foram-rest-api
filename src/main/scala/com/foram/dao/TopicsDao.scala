package com.foram.dao

import com.foram.models.Topic
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.Future

object TopicsDao extends BaseDao {
  def findAll: Future[Seq[Topic]] = db.run(topics.sortBy(_.createdAt.asc).result)

  def findLatest: Future[Seq[Topic]] = db.run(topics.sortBy(_.createdAt.desc).take(10).result)

  def findById(id: UUID): Future[Topic] = db.run(topics.filter(_.id === id).result.head)

  def findByUsername(username: String): Future[Seq[Topic]] = db.run(topics.filter(_.username === username).sortBy(_.createdAt.asc).result)

  def findByUserID(user_id: UUID): Future[Seq[Topic]] = db.run(topics.filter(_.userID === user_id).sortBy(_.createdAt.asc).result)

  def findByCategoryID(category_id: UUID): Future[Seq[Topic]] = db.run(topics.filter(_.categoryID === category_id).sortBy(_.createdAt.asc).result)

  def create(topic: Topic): Future[UUID] = db.run(topics.returning(topics.map(_.id)) += topic)

  def update(id: UUID, topic: Topic): Future[Int] = db.run(topics.filter(_.id === topic.id).update(topic))

  def delete(id: UUID): Future[Int] = db.run(topics.filter(_.id === id).delete)
}
