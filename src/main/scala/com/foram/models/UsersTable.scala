package com.foram.models

import slick.jdbc.PostgresProfile.api._

import java.util.UUID

case class User(id: UUID, name: String, username: String, email: String)

class UsersTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[UUID]("id", O.PrimaryKey)

  def name = column[String]("name")

  def username = column[String]("username")

  def email = column[String]("email")

  def * = (id, name, username, email) <> (User.tupled, User.unapply)
}
