package com.foram.actors

import akka.actor.{Actor, ActorLogging}

case class Post(id: Int, user_id: Int, username: String, topic_id: Int, topic_slug: String, post_number: Int, content: String)

object PostDB {
  case object GetAllPosts

  case class GetPost(id: Int)

  case class GetPostsByTopic(topic_id: Int)

  case class GetPostsByUser(user_id: Int)

  case class GetPostsByUsername(username: String)

  case class AddPost(post: Post)

  case class UpdatePost(id: Int, post: Post)

  case class RemovePost(post: Post)

  case object OperationSuccess
}

class PostDB extends Actor with ActorLogging {
  import PostDB._

  var posts: Map[Int, Post] = Map()

  override def receive: Receive = {
    case GetAllPosts =>
      log.info(s"Searching for posts")
      sender() ! posts.values.toList

    case GetPost(id) =>
      log.info(s"Finding post with id: $id")
      sender() ! posts.get(id)

    case GetPostsByTopic(topic_id) =>
      log.info(s"Finding posts for topic with id: $topic_id")
      sender() ! posts.values.toList.filter(x => x.topic_id == topic_id)

    case GetPostsByUser(user_id) =>
      log.info(s"Finding posts for user with id: $user_id")
      sender() ! posts.values.toList.filter(x => x.user_id == user_id)

    case GetPostsByUsername(username) =>
      log.info(s"Finding posts for user with username: $username")
      sender() ! posts.values.toList.filter(x => x.username == username)

    case AddPost(post) =>
      log.info(s"Adding post $post")
      posts = posts + (post.id -> post)
      sender() ! OperationSuccess

    case UpdatePost(id, post) =>
      log.info(s"Updating post with id: $id")
      posts = posts + (id -> post)
      sender() ! OperationSuccess

    case RemovePost(post) =>
      log.info(s"Removing post $post")
      posts = posts - post.id
      sender() ! OperationSuccess
  }
}
