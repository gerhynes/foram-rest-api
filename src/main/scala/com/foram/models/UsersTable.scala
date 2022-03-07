package com.foram.models

import slick.jdbc.PostgresProfile.api._

case class User(id: Int, name: String, username: String, email: String)

class UsersTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("id", O.PrimaryKey)

  def name = column[String]("name")

  def username = column[String]("username")

  def email = column[String]("email")

  def * = (id, name, username, email) <> (User.tupled, User.unapply)
}
