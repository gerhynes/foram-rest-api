package com.foram.dao

import com.foram.models.Post
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.Future

object PostsDao extends BaseDao {
  def findAll: Future[Seq[Post]] = db.run(posts.result)

  def findById(id: UUID): Future[Post] = db.run(posts.filter(_.id === id).result.head)

  def findByTopicID(topic_id: UUID): Future[Seq[Post]] = db.run(posts.filter(_.topicID === topic_id).result)

  def findByUserID(user_id: UUID): Future[Seq[Post]] = db.run(posts.filter(_.userID === user_id).result)

  def findByUsername(username: String): Future[Seq[Post]] = db.run(posts.filter(_.username === username).result)

  def create(post: Post): Future[UUID] = db.run(posts.returning(posts.map(_.id)) += post)

  def update(id: UUID, post: Post): Future[Int] = db.run(posts.filter(_.id === post.id).update(post))

  def delete(id: UUID): Future[Int] = db.run(posts.filter(_.id === id).delete)
}
