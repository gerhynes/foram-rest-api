package com.foram.dao

import com.foram.models.Category
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.Future

// Singleton object for database transactions
class CategoriesDao(db: PostgresProfile.backend.Database) extends BaseDao with AbstractCategoriesDao {
  def findAll: Future[Seq[Category]] = db.run(categories.sortBy(_.createdAt.asc).result)

  def findById(id: UUID): Future[Category] = db.run(categories.filter(_.id === id).result.head)

  def create(category: Category): Future[UUID] = db.run(categories.returning(categories.map(_.id)) += category)

  def update(id: UUID, category: Category): Future[Int] = db.run(categories.filter(_.id === category.id).update(category))

  def delete(id: UUID): Future[Int] = db.run(categories.filter(_.id === id).delete)
}

// Trait for mocking purposes
trait AbstractCategoriesDao extends BaseDao {
  def findAll: Future[Seq[Category]]

  def findById(id: UUID): Future[Category]

  def create(category: Category): Future[UUID]

  def update(id: UUID, category: Category): Future[Int]

  def delete(id: UUID): Future[Int]
}
