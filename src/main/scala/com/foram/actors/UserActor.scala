package com.foram.actors

import akka.actor.{Actor, ActorLogging}
import com.foram.auth.Auth.createToken
import com.foram.daos.AbstractUsersDao
import com.foram.models.{Message, RegisteredUser, User}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object UserActor {
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
      log.info(s"Searching for users")
      val usersFuture = usersDao.findAll
      val originalSender = sender
      usersFuture.onComplete {
        case Success(users) => originalSender ! users.toList
        case Failure(ex) =>
          log.info("Users not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case GetUserByID(id) =>
      log.info(s"Finding user with id: $id")
      val userFuture = usersDao.findById(id)
      val originalSender = sender
      userFuture.onComplete {
        case Success(user) => originalSender ! user
        case Failure(ex) =>
          log.info(s"User $id not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case GetUserByUsername(username) =>
      log.info(s"Finding user with username: $username")
      val userFuture = usersDao.findByUsername(username)
      val originalSender = sender
      userFuture.onComplete {
        case Success(user) => originalSender ! user
        case Failure(ex) =>
          log.info(s"User $username not found")
          ex.printStackTrace()
          originalSender ! ex
      }

    case CreateUser(user) =>
      log.info(s"Creating user ${user.username} ${user.id}")
      val userFuture = usersDao.create(user)
      val originalSender = sender
      userFuture.onComplete {
        case Success(_) =>
          // Log in created user
          val registeredUser = user match {
            case User(id, name, username, email, password, role, created_at, updated_at) => RegisteredUser(id, name, username, email, role, created_at, updated_at, createToken(username, 7) )
          }
          originalSender ! registeredUser
        case Failure(ex) =>
          log.info(s"User $user could not be created")
          ex.printStackTrace()
          originalSender ! ex
      }

    case UpdateUser(id, user) =>
      log.info(s"Updating user id ${user.id}")
      val userFuture = usersDao.update(id, user)
      val originalSender = sender
      userFuture.onComplete {
        case Success(_) => originalSender ! Message(s"User $id updated")
        case Failure(ex) =>
          log.info(s"Unable to update user $id")
          ex.printStackTrace()
          originalSender ! ex
      }

    case DeleteUser(id) =>
      log.info(s"Removing user id $id")
      val userFuture = usersDao.delete(id)
      val originalSender = sender
      userFuture.onComplete {
        case Success(_) => originalSender ! Message(s"User $id deleted")
        case Failure(ex) =>
          log.info(s"Unable to delete user $id")
          ex.printStackTrace()
          originalSender ! ex
      }
  }
}