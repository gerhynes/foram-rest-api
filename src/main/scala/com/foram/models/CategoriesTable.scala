package com.foram.models

import slick.jdbc.PostgresProfile.api._

case class Category(id: Int, name: String, slug: String, user_id: Int, description: String)

class CategoriesTable(tag: Tag) extends Table[Category](tag, "categories") {
  def id = column[Int]("id", O.PrimaryKey)

  def name = column[String]("name")

  def slug = column[String]("slug")

  def userID = column[Int]("user_id")

  def description = column[String]("description")

  def user = foreignKey("user_fk", userID, TableQuery[UsersTable])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, name, slug, userID, description) <> (Category.tupled, Category.unapply)
}