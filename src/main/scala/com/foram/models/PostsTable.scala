package com.foram.models

import slick.jdbc.PostgresProfile.api._

import java.util.UUID

case class Post(id: UUID, user_id: UUID, username: String, topic_id: UUID, topic_slug: String, post_number: Int, content: String)

class PostsTable(tag: Tag) extends Table[Post](tag, "posts") {
  def id = column[UUID]("id", O.PrimaryKey)

  def userID = column[UUID]("user_id")

  def username = column[String]("username")

  def topicID = column[UUID]("topic_id")

  def topicSlug = column[String]("topic_slug")

  def postNumber = column[Int]("post_number")

  def content = column[String]("content")

  def user = foreignKey("user_fk", userID, TableQuery[UsersTable])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def topic = foreignKey("topic_fk", topicID, TableQuery[TopicsTable])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (id, userID, username, topicID, topicSlug, postNumber, content) <> (Post.tupled, Post.unapply)
}
