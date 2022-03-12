package com.foram.models

import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime
import java.util.UUID

case class Category(id: UUID, name: String, slug: String, user_id: UUID, description: String, created_at: LocalDateTime, updated_at: LocalDateTime)

case class CategoryWithTopics(id: UUID, name: String, slug: String, user_id: UUID, description: String, created_at: LocalDateTime, updated_at: LocalDateTime, topics: List[Topic])

class CategoriesTable(tag: Tag) extends Table[Category](tag, "categories") {
  def id = column[UUID]("id", O.PrimaryKey)

  def name = column[String]("name")

  def slug = column[String]("slug")

  def userID = column[UUID]("user_id")

  def description = column[String]("description")

  def createdAt = column[LocalDateTime]("created_at")

  def updatedAt = column[LocalDateTime]("updated_at")

  def user = foreignKey("user_fk", userID, TableQuery[UsersTable])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, name, slug, userID, description, createdAt, updatedAt) <> (Category.tupled, Category.unapply)
}