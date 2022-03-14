package com.foram.models

import slick.jdbc.PostgresProfile.api._

import java.time.{LocalDateTime, OffsetDateTime}
import java.util.UUID

case class User(id: UUID, name: String, username: String, email: String, created_at: OffsetDateTime, updated_at: OffsetDateTime)

class UsersTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[UUID]("id", O.PrimaryKey)

  def name = column[String]("name")

  def username = column[String]("username")

  def email = column[String]("email")

  def createdAt = column[OffsetDateTime]("created_at")

  def updatedAt = column[OffsetDateTime]("updated_at")

  def * = (id, name, username, email, createdAt, updatedAt) <> (User.tupled, User.unapply)
}
