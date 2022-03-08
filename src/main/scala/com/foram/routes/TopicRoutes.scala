package com.foram.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.foram.Main.{postActor, topicActor}
import com.foram.actors.PostActor._
import com.foram.actors.TopicActor._
import com.foram.models.{Post, Topic, TopicWithPosts}
import spray.json.DefaultJsonProtocol._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object TopicRoutes {

  import com.foram.JsonFormats._

  implicit val timeout = Timeout(5 seconds)

  val routes =
    pathPrefix("api" / "topics") {
      get {
        path(IntNumber / "posts") { topic_id =>
          complete((postActor ? GetPostsByTopicID(topic_id)).mapTo[List[Post]])
        } ~
          path(IntNumber) { id =>
            complete((topicActor ? GetTopicByID(id)).mapTo[Topic])
          } ~
          pathEndOrSingleSlash {
            complete((topicActor ? GetAllTopics).mapTo[List[Topic]])
          }
      } ~
        post {
          entity(as[TopicWithPosts]) { topicWithPosts =>
            complete((topicActor ? CreateTopic(topicWithPosts)).map(_ => StatusCodes.OK))
          }
        } ~
        put {
          path(IntNumber) { id =>
            entity(as[Topic]) { topic =>
              complete((topicActor ? UpdateTopic(id, topic)).map(_ => StatusCodes.OK))
            }
          }
        } ~
        delete {
          path(IntNumber) { id =>
            complete((topicActor ? DeleteTopic(id)).map(_ => StatusCodes.OK))
          }
        }
    }
}