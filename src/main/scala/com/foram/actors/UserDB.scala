package com.foram.actors

import akka.actor.{Actor, ActorLogging}

case class User(id: Int, name: String, username: String, email: String)

object UserDB {
  case object GetAllUsers
  case class GetUserByUsername(username: String)
  case class GetUserById(id: Int)
  case class AddUser(user: User)
  case class UpdateUser(id: Int, user: User)
  case class RemoveUser(user: User)
  case object OperationSuccess
}

class UserDB extends Actor with ActorLogging {

  import UserDB._

  var users: Map[Int, User] = Map()

  override def receive: Receive = {
    case GetAllUsers =>
      log.info(s"Searching for users")
      sender() ! users.values.toList

    case GetUserByUsername(username) =>
      log.info(s"Finding user with username: $username")
      sender() ! users.values.toList.find(x => x.username == username)

    case GetUserById(id) =>
      log.info(s"Finding user with id: $id")
      sender() ! users.get(id)

    case AddUser(user) =>
      log.info(s"Adding user $user")
      users = users + (user.id -> user)
      sender() ! OperationSuccess

    case UpdateUser(id, user) =>
      log.info(s"Updating user with id: $id")
      users = users + (id -> user)
      sender() ! OperationSuccess

    case RemoveUser(user) =>
      log.info(s"Removing user $user")
      users = users - user.id
      sender() ! OperationSuccess
  }
}