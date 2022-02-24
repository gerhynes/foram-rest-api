package com.foram
import com.foram.actors.{Category, Topic, User, Post}
import spray.json.DefaultJsonProtocol

object JsonFormats {
  import DefaultJsonProtocol._

  implicit val categoryFormat = jsonFormat4(Category)
  implicit val topicFormat = jsonFormat4(Topic)
  implicit val userFormat = jsonFormat4(User)
  implicit val postFormat = jsonFormat5(Post)
}
