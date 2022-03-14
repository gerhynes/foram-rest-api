package com.foram.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.pattern.ask
import akka.util.Timeout
import com.foram.Main.{postActor, topicActor}
import com.foram.actors.PostActor._
import com.foram.actors.TopicActor._
import com.foram.models.{Post, Topic, NewTopic}
import spray.json.DefaultJsonProtocol._

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object TopicRoutes {

  import com.foram.JsonFormats._

  implicit val timeout = Timeout(5 seconds)

  val routes =
    pathPrefix("api" / "topics") {
      get {
        path(Segment / "posts") { topic_id =>
          val uuid = UUID.fromString(topic_id)
          complete((postActor ? GetPostsByTopicID(uuid)).mapTo[List[Post]])
        } ~
          path(Segment) { id =>
            val uuid = UUID.fromString(id)
            complete((topicActor ? GetTopicByID(uuid)).mapTo[Topic])
          } ~
          pathEndOrSingleSlash {
            complete((topicActor ? GetAllTopics).mapTo[List[Topic]])
          }
      } ~
        post {
          entity(as[NewTopic]) { newTopic =>
            complete((topicActor ? CreateTopic(newTopic)).map(_ => StatusCodes.Created))
          }
        } ~
        put {
          path(Segment) { id =>
            val uuid = UUID.fromString(id)
            entity(as[Topic]) { topic =>
              complete((topicActor ? UpdateTopic(uuid, topic)).map(_ => StatusCodes.OK))
            }
          }
        } ~
        delete {
          path(Segment) { id =>
            val uuid = UUID.fromString(id)
            complete((topicActor ? DeleteTopic(uuid)).map(_ => StatusCodes.OK))
          }
        }
    }
}

