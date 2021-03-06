package com.foram.daos

import com.foram.auth.Auth
import com.foram.models.{User, UsersTable}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.util.UUID
import scala.concurrent.Future

class UsersDao(db: PostgresProfile.backend.Database) extends AbstractUsersDao {
  def findAll: Future[Seq[User]] = db.run(users.sortBy(_.createdAt.asc).result)

  def findById(id: UUID): Future[User] = db.run(users.filter(_.id === id).result.head)

  def findByUsername(username: String): Future[User] = db.run(users.filter(_.username === username).result.head)

  def create(user: User): Future[UUID] = db.run(users.returning(users.map(_.id)) += Auth.hashUserPassword(user))

  def update(id: UUID, user: User): Future[Int] = db.run(users.filter(_.id === user.id).update(user))

  def delete(id: UUID): Future[Int] = db.run(users.filter(_.id === id).delete)
}

// Trait for mocking purposes
trait AbstractUsersDao {
  val users = TableQuery[UsersTable]

  def findAll: Future[Seq[User]]

  def findById(id: UUID): Future[User]

  def findByUsername(username: String): Future[User]

  def create(user: User): Future[UUID]

  def update(id: UUID, user: User): Future[Int]

  def delete(id: UUID): Future[Int]
}