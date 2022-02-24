package com.foram.actors

import akka.actor.{Actor, ActorLogging}

case class Category(id: Int, name: String, user_id: Int, description: String)

object CategoryDB {
  case object GetAllCategories

  case class GetCategory(id: Int)

  case class AddCategory(category: Category)

  case class UpdateCategory(id: Int, category: Category)

  case class RemoveCategory(category: Category)

  case object OperationSuccess
}

class CategoryDB extends Actor with ActorLogging {

  import CategoryDB._

  var categories: Map[Int, Category] = Map()

  override def receive: Receive = {
    case GetAllCategories =>
      log.info(s"Searching for categories")
      sender() ! categories.values.toList

    case GetCategory(id) =>
      log.info(s"Finding category with id: $id")
      sender() ! categories.get(id)

    case AddCategory(category) =>
      log.info(s"Adding category $category")
      categories = categories + (category.id -> category)
      sender() ! OperationSuccess

    case UpdateCategory(id, category) =>
      log.info(s"Updating category $category")
      categories = categories + (id -> category)
      sender() ! OperationSuccess

    case RemoveCategory(category) =>
      log.info(s"Removing category $category")
      categories = categories - category.id
      sender() ! OperationSuccess
  }
}
