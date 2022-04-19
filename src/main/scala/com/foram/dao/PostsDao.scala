package com.foram.dao

import com.foram.models.{Post, PostsTable}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.util.UUID
import scala.concurrent.Future

class PostsDao(db: PostgresProfile.backend.Database) extends AbstractPostsDao {
  def findAll: Future[Seq[Post]] = db.run(posts.sortBy(_.createdAt.asc).result)

  def findById(id: UUID): Future[Post] = db.run(posts.filter(_.id === id).result.head)

  def findByTopicID(topic_id: UUID): Future[Seq[Post]] = db.run(posts.filter(_.topicID === topic_id).sortBy(_.createdAt.asc).result)

  def findByUserID(user_id: UUID): Future[Seq[Post]] = db.run(posts.filter(_.userID === user_id).sortBy(_.createdAt.asc).result)

  def findByUsername(username: String): Future[Seq[Post]] = db.run(posts.filter(_.username === username).sortBy(_.createdAt.asc).result)

  def create(post: Post): Future[UUID] = db.run(posts.returning(posts.map(_.id)) += post)

  def update(id: UUID, post: Post): Future[Int] = db.run(posts.filter(_.id === post.id).update(post))

  def delete(id: UUID): Future[Int] = db.run(posts.filter(_.id === id).delete)
}

// trait for mocking purposes
trait AbstractPostsDao {
  val posts = TableQuery[PostsTable]

  def findAll: Future[Seq[Post]]

  def findById(id: UUID): Future[Post]

  def findByTopicID(topic_id: UUID): Future[Seq[Post]]

  def findByUserID(user_id: UUID): Future[Seq[Post]]

  def findByUsername(username: String): Future[Seq[Post]]

  def create(post: Post): Future[UUID]

  def update(id: UUID, post: Post): Future[Int]

  def delete(id: UUID): Future[Int]
}


