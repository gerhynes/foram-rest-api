package com.foram.json

import com.foram.models._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat, deserializationError}

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.util.Try

object JsonFormats {

  import DefaultJsonProtocol._

  // Implement support for UUIDs (not included in spray-json)
  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID): JsString = JsString(uuid.toString)

    def read(value: JsValue): UUID = {
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
        case JsString(lTString) => Try(OffsetDateTime.parse(lTString, formatter)).getOrElse(deserializationError(deserializationErrorMessage))
        case _ => deserializationError(deserializationErrorMessage)
      }
    }

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val deserializationErrorMessage = s"Expected date time in ISO offset local date time format ex. ${OffsetDateTime.now().format(formatter)}"
  }

  implicit val categoryFormat: RootJsonFormat[Category] = jsonFormat7(Category)
  implicit val topicFormat: RootJsonFormat[Topic] = jsonFormat9(Topic)
  implicit val postFormat: RootJsonFormat[Post] = jsonFormat9(Post)
  implicit val newTopicFormat: RootJsonFormat[TopicWithChildren] = jsonFormat10(TopicWithChildren)
  implicit val newCategoryFormat: RootJsonFormat[CategoryWithChildren] = jsonFormat8(CategoryWithChildren)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat8(User)
  implicit val registeredUserFormat: RootJsonFormat[RegisteredUser] = jsonFormat8(RegisteredUser)
  implicit val loginRequestFormat: RootJsonFormat[LoginRequest] = jsonFormat2(LoginRequest)
  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat1(Message)
}
