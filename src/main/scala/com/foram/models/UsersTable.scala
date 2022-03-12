package com.foram.models

import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime
import java.util.UUID

case class User(id: UUID, name: String, username: String, email: String, created_at: LocalDateTime, updated_at: LocalDateTime)

class UsersTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[UUID]("id", O.PrimaryKey)

  def name = column[String]("name")

  def username = column[String]("username")

  def email = column[String]("email")

  def createdAt = column[LocalDateTime]("created_at")

  def updatedAt = column[LocalDateTime]("updated_at")

  def * = (id, name, username, email, createdAt, updatedAt) <> (User.tupled, User.unapply)
}
