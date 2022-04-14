package com.foram.models

import slick.jdbc.PostgresProfile.api._

import java.time.OffsetDateTime
import java.util.UUID

case class LoginRequest(username: String, password: String)

case class Message(message: String)

case class User(id: UUID, name: String, username: String, email: String, password: String, role: String, created_at: OffsetDateTime, updated_at: OffsetDateTime)

case class RegisteredUser(id: UUID, name: String, username: String, email: String, role: String, created_at: OffsetDateTime, updated_at: OffsetDateTime, token: String)

class UsersTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[UUID]("id", O.PrimaryKey)

  def name = column[String]("name")

  def username = column[String]("username")

  def email = column[String]("email")

  def password = column[String]("password")

  def role = column[String]("role")

  def createdAt = column[OffsetDateTime]("created_at")

  def updatedAt = column[OffsetDateTime]("updated_at")

  def * = (id, name, username, email, password, role, createdAt, updatedAt) <> (User.tupled, User.unapply)
}
