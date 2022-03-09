package com.foram.dao

import com.foram.models.User
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.Future

object UsersDao extends BaseDao {
  def findAll: Future[Seq[User]] = db.run(users.result)

  def findById(id: UUID): Future[User] = db.run(users.filter(_.id === id).result.head)

  def findByUsername(username: String): Future[User] = db.run(users.filter(_.username === username).result.head)

  def create(user: User): Future[UUID] = db.run(users.returning(users.map(_.id)) += user)

  def update(id: UUID, user: User): Future[Int] = db.run(users.filter(_.id === user.id).update(user))

  def delete(id: UUID): Future[Int] = db.run(users.filter(_.id === id).delete)
}
