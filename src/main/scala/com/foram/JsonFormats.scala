package com.foram

import com.foram.models._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, deserializationError}

import java.time.OffsetDateTime
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

  // Implement support for OffsetDateTime (not included in spray-json)
  implicit val offsetDateTimeFormat = new JsonFormat[OffsetDateTime] {
    override def write(obj: OffsetDateTime): JsValue = JsString(formatter.format(obj))

    override def read(json: JsValue): OffsetDateTime = {
      json match {
        case JsString(lTString) =>
          Try(OffsetDateTime.parse(lTString, formatter)).getOrElse(deserializationError(deserializationErrorMessage))
        case _ => deserializationError(deserializationErrorMessage)
      }
    }

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val deserializationErrorMessage =
      s"Expected date time in ISO offset local date time format ex. ${OffsetDateTime.now().format(formatter)}"
  }

  implicit val categoryFormat = jsonFormat7(Category)
  implicit val topicFormat = jsonFormat9(Topic)
  implicit val postFormat = jsonFormat9(Post)
  implicit val newCategoryFormat = jsonFormat9(NewCategory)
  implicit val newTopicFormat = jsonFormat10(NewTopic)
  implicit val userFormat = jsonFormat8(User)
  implicit val loginRequestFormat = jsonFormat2(LoginRequest)
}


