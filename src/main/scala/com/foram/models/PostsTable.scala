package com.foram.models

import slick.jdbc.PostgresProfile.api._

case class Post(id: Int, user_id: Int, username: String, topic_id: Int, topic_slug: String, post_number: Int, content: String)

class PostsTable(tag: Tag) extends Table[Post](tag, "posts") {
  def id = column[Int]("id", O.PrimaryKey)

  def userID = column[Int]("user_id")

  def username = column[String]("username")

  def topicID = column[Int]("topic_id")

  def topicSlug = column[String]("topic_slug")

  def postNumber = column[Int]("post_number")

  def content = column[String]("content")

  def user = foreignKey("user_fk", userID, TableQuery[UsersTable])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def topic = foreignKey("topic_fk", topicID, TableQuery[TopicsTable])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (id, userID, username, topicID, topicSlug, postNumber, content) <> (Post.tupled, Post.unapply)
}
