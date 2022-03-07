package com.foram.dao

import com.foram.models.{CategoriesTable, PostsTable, TopicsTable, UsersTable}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

trait BaseDao {
  def db = Database.forConfig("postgresDB")

  val categories = TableQuery[CategoriesTable]
  val users = TableQuery[UsersTable]
  val topics = TableQuery[TopicsTable]
  val posts = TableQuery[PostsTable]
}
