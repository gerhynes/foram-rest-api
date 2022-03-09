package com.foram

import com.foram.models._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}

import java.util.UUID

object JsonFormats {

  import DefaultJsonProtocol._

  // Implement support for UUIDs (not included in spray-json)
  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)

    def read(value: JsValue) = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ => throw new DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }

  implicit val categoryFormat = jsonFormat5(Category)
  implicit val topicFormat = jsonFormat7(Topic)
  implicit val postFormat = jsonFormat7(Post)
  implicit val categoryWithTopicsFormat = jsonFormat6(CategoryWithTopics)
  implicit val topicWithPostsFormat = jsonFormat8(TopicWithPosts)
  implicit val userFormat = jsonFormat4(User)
}


