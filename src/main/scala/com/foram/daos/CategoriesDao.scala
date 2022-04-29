package com.foram.daos

import com.foram.models.{CategoriesTable, Category}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import java.util.UUID
import scala.concurrent.Future

class CategoriesDao(db: PostgresProfile.backend.Database) extends AbstractCategoriesDao {
  def findAll: Future[Seq[Category]] = db.run(categories.sortBy(_.createdAt.asc).result)

  def findById(id: UUID): Future[Category] = db.run(categories.filter(_.id === id).result.head)

  def create(category: Category): Future[UUID] = db.run(categories.returning(categories.map(_.id)) += category)

  def update(id: UUID, category: Category): Future[Int] = db.run(categories.filter(_.id === category.id).update(category))

  def delete(id: UUID): Future[Int] = db.run(categories.filter(_.id === id).delete)
}

// Trait for mocking purposes
trait AbstractCategoriesDao {
  val categories = TableQuery[CategoriesTable]

  def findAll: Future[Seq[Category]]

  def findById(id: UUID): Future[Category]

  def create(category: Category): Future[UUID]

  def update(id: UUID, category: Category): Future[Int]

  def delete(id: UUID): Future[Int]
}
