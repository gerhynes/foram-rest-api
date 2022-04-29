package com.foram.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.foram.daos.{AbstractCategoriesDao, AbstractPostsDao, AbstractTopicsDao}
import com.foram.models.{Category, CategoryWithChildren, Message, Topic, TopicWithChildren}

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
      log.info(s"Searching for categories")
      val categoriesFuture = categoriesDao.findAll
      val originalSender = sender
      categoriesFuture.onComplete {
        case Success(categories) => originalSender ! categories.toList
        case Failure(ex) =>
          log.info("Categories not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case GetCategoryByID(id) =>
      log.info(s"Finding category with id: $id")
      val categoryFuture = categoriesDao.findById(id)
      val originalSender = sender
      categoryFuture.onComplete {
        case Success(category) => originalSender ! category
        case Failure(ex) =>
          log.info(s"Category $id not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case CreateCategory(newCategory) =>
      log.info(s"Creating new category from $newCategory")

      // Separate category from topic
      val category = newCategory match {
        case CategoryWithChildren(id, name, slug, user_id, description, created_at, updated_at, topics) =>
          Category(id, name, slug, user_id, description, created_at, updated_at)
      }
      // Separate post from topic
      val newTopic = newCategory.topics.head
      val topic = newTopic match {
        case TopicWithChildren(id, title, slug, user_id, username, category_id, category_name, created_at, updated_at, posts) =>
          Topic(id, title, slug, user_id, username, category_id, category_name, created_at, updated_at)
      }
      val post = newTopic.posts.head

      val originalSender = sender

      // Save category, topic and post to database
      // Chain futures to prevent foreign key constraint violation
      val newCategoryFuture = categoriesDao.create(category).flatMap(_ => topicsDao.create(topic)).flatMap(_ => postsDao.create(post))

      newCategoryFuture.onComplete {
        case Success(_) => originalSender ! category
        case Failure(ex) =>
          log.info(s"Unable to create category $category")
          ex.printStackTrace()
          originalSender ! ex
      }

    case UpdateCategory(id, category) =>
      log.info(s"Updating category $category")
      val categoryFuture = categoriesDao.update(id, category)
      val originalSender = sender
      categoryFuture.onComplete {
        case Success(_) => originalSender ! Message(s"Category $id updated")
        case Failure(ex) =>
          log.info(s"Unable to update category $id")
          ex.printStackTrace()
          originalSender ! ex
      }

    case DeleteCategory(id) =>
      log.info(s"Removing category id $id")
      val categoryFuture = categoriesDao.delete(id)
      val originalSender = sender
      categoryFuture.onComplete {
        case Success(_) => originalSender ! Message(s"Category $id deleted")
        case Failure(ex) =>
          log.info(s"Unable to delete category id $id")
          ex.printStackTrace()
          originalSender ! ex
      }
  }
}