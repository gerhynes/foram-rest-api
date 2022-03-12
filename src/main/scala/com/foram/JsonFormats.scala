package com.foram

import com.foram.models._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}

import java.time.LocalTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.util.Try

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

  // Implement support for LocalDateTime (not included in spray-json)
  implicit val localDateTimeFormat = new JsonFormat[LocalDateTime] {
    override def write(obj: LocalDateTime): JsValue = JsString(formatter.format(obj))

    override def read(json: JsValue): LocalDateTime = {
      json match {
        case JsString(lTString) =>
          Try(LocalDateTime.parse(lTString, formatter)).getOrElse(deserializationError(deserializationErrorMessage))
        case _ => deserializationError(deserializationErrorMessage)
      }
    }

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val deserializationErrorMessage =
      s"Expected local date time in ISO offset local date time format ex. ${LocalDateTime.now().format(formatter)}"
  }

  implicit val categoryFormat = jsonFormat7(Category)
  implicit val topicFormat = jsonFormat9(Topic)
  implicit val postFormat = jsonFormat9(Post)
  implicit val categoryWithTopicsFormat = jsonFormat8(CategoryWithTopics)
  implicit val topicWithPostsFormat = jsonFormat10(TopicWithPosts)
  implicit val userFormat = jsonFormat6(User)
}


