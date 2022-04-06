package com.foram.actors

import akka.actor.{Actor, ActorLogging}
import com.foram.auth.Auth
import com.foram.dao.{AbstractUsersDao, UsersDao}
import com.foram.models.User

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object UserActor {
  case class ActionPerformed(action: String)

  case object GetAllUsers

  case class GetUserByID(id: UUID)

  case class GetUserByUsername(username: String)

  case class CreateUser(user: User)

  case class UpdateUser(id: UUID, user: User)

  case class DeleteUser(id: UUID)
}


class UserActor(usersDao: AbstractUsersDao) extends Actor with ActorLogging {

  import UserActor._

  override def receive: Receive = {
    case GetAllUsers =>
      println(s"Searching for users")
      val usersFuture = usersDao.findAll
      val originalSender = sender
      usersFuture.onComplete {
        case Success(users) => originalSender ! users.toList
        case Failure(e) =>
          println("Users not found")
          e.printStackTrace()
          originalSender ! e
      }

    case GetUserByID(id) =>
      println(s"Finding user with id: $id")
      val userFuture = usersDao.findById(id)
      val originalSender = sender
      userFuture.onComplete {
        case Success(user) => originalSender ! user
        case Failure(e) =>
          println(s"User $id not found")
          e.printStackTrace()
          originalSender ! e
      }

    case GetUserByUsername(username) =>
      println(s"Finding user with username: $username")
      val userFuture = usersDao.findByUsername(username)
      val originalSender = sender
      userFuture.onComplete {
        case Success(user) => originalSender ! user
        case Failure(e) =>
          println(s"User $username not found")
          e.printStackTrace()
          originalSender ! e
      }

    case CreateUser(rawUser) =>
      println(s"Creating user $rawUser")

      // Create new user with hashed password
      val user = rawUser match {
        case User(id, name, username, email, password, role, created_at, updated_at) => User(id, name, username, email, Auth.hashPassword(rawUser.password), role, created_at, updated_at)
      }

      val userFuture = usersDao.create(user)
      val originalSender = sender
      userFuture.onComplete {
        case Success(user) => originalSender ! ActionPerformed(s"User ${user} created.")
        case Failure(e) =>
          println(s"User $user could not be created")
          e.printStackTrace()
          originalSender ! e
      }

    case UpdateUser(id, user) =>
      println(s"Updating user $user")
      val userFuture = usersDao.update(id, user)
      val originalSender = sender
      userFuture.onComplete {
        case Success(success) => originalSender ! ActionPerformed(s"User $id updated")
        case Failure(e) =>
          println(s"Unable to update user $id")
          e.printStackTrace()
          originalSender ! e
      }

    case DeleteUser(id) =>
      println(s"Removing user id $id")
      val userFuture = usersDao.delete(id)
      val originalSender = sender
      userFuture.onComplete {
        case Success(success) => originalSender ! ActionPerformed(s"User $id deleted")
        case Failure(e) =>
          println(s"Unable to delete user $id")
          e.printStackTrace()
          originalSender ! e
      }
  }
}