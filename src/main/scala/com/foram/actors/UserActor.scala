package com.foram.actors

import akka.actor.{Actor, ActorLogging}
import com.foram.dao.UsersDao
import com.foram.models.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object UserActor {
  case class ActionPerformed(action: String)

  case object GetAllUsers

  case class GetUserByID(id: Int)

  case class GetUserByUsername(username: String)

  case class CreateUser(user: User)

  case class UpdateUser(id: Int, user: User)

  case class DeleteUser(id: Int)
}


class UserActor extends Actor with ActorLogging {

  import UserActor._

  override def receive: Receive = {
    case GetAllUsers =>
      println(s"Searching for users")
      val usersFuture = UsersDao.findAll
      val originalSender = sender
      usersFuture.onComplete {
        case Success(users) => originalSender ! users.toList
        case Failure(e) =>
          println("Users not found")
          e.printStackTrace()
      }

    case GetUserByID(id) =>
      println(s"Finding user with id: $id")
      val userFuture = UsersDao.findById(id)
      val originalSender = sender
      userFuture.onComplete {
        case Success(user) => originalSender ! user
        case Failure(e) =>
          println(s"User $id not found")
          e.printStackTrace()
      }

    case GetUserByUsername(username) =>
      println(s"Finding user with username: $username")
      val userFuture = UsersDao.findByUsername(username)
      val originalSender = sender
      userFuture.onComplete {
        case Success(user) => originalSender ! user
        case Failure(e) =>
          println(s"User $username not found")
          e.printStackTrace()
      }

    case CreateUser(user) =>
      println(s"Creating user $user")
      val userFuture = UsersDao.create(user)
      val originalSender = sender
      userFuture.onComplete {
        case Success(user) => originalSender ! ActionPerformed(s"User ${user} created.")
        case Failure(e) =>
          println(s"User $user could not be created")
          e.printStackTrace()
      }

    case UpdateUser(id, user) =>
      println(s"Updating user $user")
      val userFuture = UsersDao.update(id, user)
      val originalSender = sender
      userFuture.onComplete {
        case Success(success) => originalSender ! ActionPerformed(s"User $id updated")
        case Failure(e) =>
          println(s"Unable to update user $id")
          e.printStackTrace()
      }

    case DeleteUser(id) =>
      println(s"Removing user id $id")
      val userFuture = UsersDao.delete(id)
      val originalSender = sender
      userFuture.onComplete {
        case Success(success) => originalSender ! ActionPerformed(s"User $id deleted")
        case Failure(e) =>
          println(s"Unable to delete user $id")
          e.printStackTrace()
      }
  }
}