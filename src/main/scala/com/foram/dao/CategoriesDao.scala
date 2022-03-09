package com.foram.dao

import com.foram.models.Category
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.Future

object CategoriesDao extends BaseDao {
  def findAll: Future[Seq[Category]] = db.run(categories.result)

  def findById(id: UUID): Future[Category] = db.run(categories.filter(_.id === id).result.head)

  def create(category: Category): Future[UUID] = db.run(categories.returning(categories.map(_.id)) += category)

  def update(id: UUID, category: Category): Future[Int] = db.run(categories.filter(_.id === category.id).update(category))

  def delete(id: UUID): Future[Int] = db.run(categories.filter(_.id === id).delete)
}
