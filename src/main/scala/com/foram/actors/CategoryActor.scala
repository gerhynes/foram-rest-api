package com.foram.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.foram.dao.{AbstractCategoriesDao, AbstractPostsDao, AbstractTopicsDao}
import com.foram.models.{Category, CategoryWithChildren, Message}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object CategoryActor {
  case object GetAllCategories

  case class GetCategoryByID(id: UUID)

  case class CreateCategory(newCategory: CategoryWithChildren)

  case class UpdateCategory(id: UUID, category: Category)

  case class DeleteCategory(id: UUID)

  def props = Props[CategoryActor]
}

class CategoryActor(categoriesDao: AbstractCategoriesDao, topicsDao: AbstractTopicsDao, postsDao: AbstractPostsDao) extends Actor with ActorLogging {

  import CategoryActor._

  override def receive: Receive = {
    case GetAllCategories =>
      println(s"Searching for categories")
      val categoriesFuture = categoriesDao.findAll
      val originalSender = sender
      categoriesFuture.onComplete {
        case Success(categories) => originalSender ! categories.toList
        case Failure(e) =>
          println("Categories not found")
          e.printStackTrace()
          originalSender ! e
      }

    case GetCategoryByID(id) =>
      println(s"Finding category with id: $id")
      val categoryFuture = categoriesDao.findById(id)
      val originalSender = sender
      categoryFuture.onComplete {
        case Success(category) => originalSender ! category
        case Failure(e) =>
          println(s"Category $id not found")
          e.printStackTrace()
          originalSender ! e
      }

    case CreateCategory(newCategory) =>
      println(s"Creating new category from $newCategory")

      // Separate category, topic and post
      val category = newCategory match {
        case CategoryWithChildren(id, name, slug, user_id, description, created_at, updated_at, topics, posts) => Category(id, name, slug, user_id, description, created_at, updated_at)
      }
      val topic = newCategory.topics.head
      val post = newCategory.posts.head

      val originalSender = sender

      // Save category, topic and post to database
      val categoryFuture = categoriesDao.create(category)
      val topicFuture = topicsDao.create(topic)
      val postFuture = postsDao.create(post)

      // Get results of all futures
      val result = for {
        category_id <- categoryFuture
        topic_id <- topicFuture
        post_id <- postFuture
      } yield (category_id, topic_id, post_id)

      result.onComplete {
        case Success(result) => originalSender ! category
        case Failure(e) =>
          println(s"Unable to create category $category")
          e.printStackTrace()
          originalSender ! e
      }

    case UpdateCategory(id, category) =>
      println(s"Updating category $category")
      val categoryFuture = categoriesDao.update(id, category)
      val originalSender = sender
      categoryFuture.onComplete {
        case Success(success) => originalSender ! Message(s"Category $id updated")
        case Failure(e) =>
          println(s"Unable to update category $id")
          e.printStackTrace()
          originalSender ! e
      }

    case DeleteCategory(id) =>
      println(s"Removing category id $id")
      val categoryFuture = categoriesDao.delete(id)
      val originalSender = sender
      categoryFuture.onComplete {
        case Success(success) => originalSender ! Message(s"Category $id deleted")
        case Failure(e) =>
          println(s"Unable to delete category id $id")
          e.printStackTrace()
          originalSender ! e
      }
  }
}