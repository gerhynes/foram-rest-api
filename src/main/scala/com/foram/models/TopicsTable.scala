package com.foram.models

import slick.jdbc.PostgresProfile.api._

case class Topic(id: Int, title: String, slug: String, user_id: Int, username: String, category_id: Int, category_name: String)

class TopicsTable(tag: Tag) extends Table[Topic](tag, "topics") {
  def id = column[Int]("id", O.PrimaryKey)

  def title = column[String]("title")

  def slug = column[String]("slug")

  def userID = column[Int]("user_id")

  def username = column[String]("username")

  def categoryID = column[Int]("category_id")

  def categoryName = column[String]("category_name")

  def user = foreignKey("user_fk", userID, TableQuery[UsersTable])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def category = foreignKey("category_fk", categoryID, TableQuery[CategoriesTable])(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (id, title, slug, userID, username, categoryID, categoryName) <> (Topic.tupled, Topic.unapply)
}
