package com.foram

import com.foram.models._
import spray.json.DefaultJsonProtocol

object JsonFormats {

  import DefaultJsonProtocol._

  implicit val categoryFormat = jsonFormat5(Category)
  implicit val topicFormat = jsonFormat7(Topic)
  implicit val postFormat = jsonFormat7(Post)
  implicit val topicWithPostsFormat = jsonFormat8(TopicWithPosts)
  implicit val userFormat = jsonFormat4(User)

}
