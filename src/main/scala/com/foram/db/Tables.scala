package com.foram.db
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, ForeignKeyQuery}

// A Users table with 4 columns: id, name, username, email
class Users(tag: Tag) extends Table[(Int, String, String, String)](tag, "users") {
  def id = column[Int]("id", O.PrimaryKey)
  def name = column[String]("name")
  def username = column[String]("username")
  def email = column[String]("email")
  def * = (id, name, username, email)
}

// A Categories table with 5 columns: id, name, slug, userID, description
class Categories(tag: Tag) extends Table[(Int, String, String, Int, String)](tag, "categories") {
  def id = column[Int]("id", O.PrimaryKey)
  def name = column[String]("name")
  def slug = column[String]("slug")
  def userID = column[Int]("user_id")
  def description = column[String]("description")
  def user = foreignKey("user_fk", userID, TableQuery[Users])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, name, slug, userID, description)
}

// A Topics table with 6 fields: id, title, slug, userID, categoryID and categoryName
class Topics(tag: Tag) extends Table[(Int, String, String, Int, String, Int, String)](tag, "topics") {
  def id = column[Int]("id", O.PrimaryKey)
  def title = column[String]("title")
  def slug = column[String]("slug")
  def userID = column[Int]("user_id")
  def username = column[String]("username")
  def categoryID = column[Int]("category_id")
  def categoryName= column[String]("category_name")
  def user = foreignKey("user_fk", userID, TableQuery[Users])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def category = foreignKey("category_fk", categoryID, TableQuery[Categories])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def * = (id, title, slug, userID, username, categoryID, categoryName)
}

// A Posts table with 6 fields: id, userID, username, topicID, postNumber, content
class Posts(tag: Tag) extends Table[(Int, Int, String, Int, String, Int, String)](tag, "posts") {
  def id = column[Int]("id", O.PrimaryKey)
  def userID = column[Int]("user_id")
  def username = column[String]("username")
  def topicID = column[Int]("topic_id")
  def topicSlug = column[String]("topic_slug")
  def postNumber = column[Int]("post_number")
  def content = column[String]("content")
  def user = foreignKey("user_fk", userID, TableQuery[Users])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def topic = foreignKey("topic_fk", topicID, TableQuery[Topics])(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def * = (id, userID, username, topicID, topicSlug, postNumber, content)
}

