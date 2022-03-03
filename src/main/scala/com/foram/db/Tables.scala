package com.foram.db
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, ForeignKeyQuery}

// A Users table with 4 columns: id, name, username, email
class Users(tag: Tag) extends Table[(Int, String, String, String)](tag, "USERS") {
  def id = column[Int]("ID", O.PrimaryKey)
  def name = column[String]("NAME")
  def username = column[String]("USERNAME")
  def email = column[String]("EMAIL")
  def * = (id, name, username, email)
}

// A Categories table with 5 columns: id, name, slug, userID, description
class Categories(tag: Tag) extends Table[(Int, String, String, Int, String)](tag, "CATEGORIES") {
  def id = column[Int]("ID", O.PrimaryKey)
  def name = column[String]("NAME")
  def slug = column[String]("SLUG")
  def userID = column[Int]("USER_ID")
  def description = column[String]("DESCRIPTION")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, name, slug, userID, description)
}

// A Topics table with 6 fields: id, title, slug, userID, categoryID and categoryName
class Topics(tag: Tag) extends Table[(Int, String, String, Int, String, Int, String)](tag, "TOPICS") {
  def id = column[Int]("ID", O.PrimaryKey)
  def title = column[String]("TITLE")
  def slug = column[String]("SLUG")
  def userID = column[Int]("USER_ID")
  def username = column[String]("USERNAME")
  def categoryID = column[Int]("CATEGORY_ID")
  def categoryName= column[String]("CATEGORY_NAME")
  // TODO handle foreign keys
  def * = (id, title, slug, userID, username, categoryID, categoryName)
}

// A Posts table with 6 fields: id, userID, username, topicID, postNumber, content
class Posts(tag: Tag) extends Table[(Int, Int, String, Int, String, Int, String)](tag, "POSTS") {
  def id = column[Int]("ID", O.PrimaryKey)
  def userID = column[Int]("USER_ID")
  def username = column[String]("USERNAME")
  def topicID = column[Int]("TOPIC_ID")
  def topicSlug = column[String]("TOPIC_SLUG")
  def postNumber = column[Int]("POST_NUMBER")
  def content = column[String]("CONTENT")
  // TODO handle foreign keys
  def * = (id, userID, username, topicID, topicSlug, postNumber, content)
}