package com.foram.models

import slick.jdbc.PostgresProfile.api._

import java.time.OffsetDateTime
import java.util.UUID

case class Topic(id: UUID, title: String, slug: String, user_id: UUID, username: String, category_id: UUID, category_name: String, created_at: OffsetDateTime, updated_at: OffsetDateTime)

case class NewTopic(id: UUID, title: String, slug: String, user_id: UUID, username: String, category_id: UUID, category_name: String, created_at: OffsetDateTime, updated_at: OffsetDateTime, posts: List[Post])

class TopicsTable(tag: Tag) extends Table[Topic](tag, "topics") {
  def id = column[UUID]("id", O.PrimaryKey)

  def title = column[String]("title")

  def slug = column[String]("slug")

  def userID = column[UUID]("user_id")

  def username = column[String]("username")

  def categoryID = column[UUID]("category_id")

  def categoryName = column[String]("category_name")

  def createdAt = column[OffsetDateTime]("created_at")

  def updatedAt = column[OffsetDateTime]("updated_at")

  def user = foreignKey("user_fk", userID, TableQuery[UsersTable])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def category = foreignKey("category_fk", categoryID, TableQuery[CategoriesTable])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (id, title, slug, userID, username, categoryID, categoryName, createdAt, updatedAt) <> (Topic.tupled, Topic.unapply)
}
