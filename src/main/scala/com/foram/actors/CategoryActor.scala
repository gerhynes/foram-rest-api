package com.foram.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.foram.dao.{CategoriesDao, TopicsDao}
import com.foram.models.{Category, CategoryWithTopics}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object CategoryActor {
  case class ActionPerformed(action: String)

  case object GetAllCategories

  case class GetCategoryByID(id: UUID)

  case class CreateCategory(categoryWithTopics: CategoryWithTopics)

  case class UpdateCategory(id: UUID, category: Category)

  case class DeleteCategory(id: UUID)

  def props = Props[CategoryActor]
}

class CategoryActor extends Actor with ActorLogging {

  import CategoryActor._

  override def receive: Receive = {
    case GetAllCategories =>
      println(s"Searching for categories")
      val categoriesFuture = CategoriesDao.findAll
      val originalSender = sender
      categoriesFuture.onComplete {
        case Success(categories) => originalSender ! categories.toList
        case Failure(e) =>
          println("Categories not found")
          e.printStackTrace()
      }

    case GetCategoryByID(id) =>
      println(s"Finding category with id: $id")
      val categoryFuture = CategoriesDao.findById(id)
      val originalSender = sender
      categoryFuture.onComplete {
        case Success(category) => originalSender ! category
        case Failure(e) =>
          println(s"Category $id not found")
          e.printStackTrace()
      }

    case CreateCategory(categoryWithTopics) =>
      println(s"Creating category and topic from $categoryWithTopics")

      // Separate category and topic
      val category = categoryWithTopics match {
        case CategoryWithTopics(id, name, slug, user_id, description, topics) => Category(id, name, slug, user_id, description)
      }
      val topic = categoryWithTopics.topics.head

      val originalSender = sender

      // Save category and topic to database
      val categoryFuture = CategoriesDao.create(category)
      val topicFuture = TopicsDao.create(topic)

      // Get results of both futures
      val result = for {
        category_id <- categoryFuture
        topic_id <- topicFuture
      } yield (category_id, topic_id)

      result.onComplete {
        case Success(result) => originalSender ! ActionPerformed(s"Category $result._1 and topic $result._2 created.")
        case Failure(e) =>
          println(s"Unable to create category $category")
          e.printStackTrace()
      }

    case UpdateCategory(id, category) =>
      println(s"Updating category $category")
      val categoryFuture = CategoriesDao.update(id, category)
      val originalSender = sender
      categoryFuture.onComplete {
        case Success(success) => originalSender ! ActionPerformed(s"Category $id updated")
        case Failure(e) =>
          println(s"Unable to update category $id")
          e.printStackTrace()
      }

    case DeleteCategory(id) =>
      println(s"Removing category id $id")
      val categoryFuture = CategoriesDao.delete(id)
      val originalSender = sender
      categoryFuture.onComplete {
        case Success(success) => originalSender ! ActionPerformed(s"Category id $id deleted")
        case Failure(e) =>
          println(s"Unable to delete category id $id")
          e.printStackTrace()
      }
  }
}