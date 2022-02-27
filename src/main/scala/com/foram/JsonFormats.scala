package com.foram
import com.foram.actors.{Category, Topic, User, Post}
import spray.json.DefaultJsonProtocol

object JsonFormats {
  import DefaultJsonProtocol._

  implicit val categoryFormat = jsonFormat5(Category)
  implicit val topicFormat = jsonFormat6(Topic)
  implicit val userFormat = jsonFormat4(User)
  implicit val postFormat = jsonFormat6(Post)
}
