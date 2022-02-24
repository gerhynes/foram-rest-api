package com.foram.routes

import scala.concurrent._
import ExecutionContext.Implicits.global
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import akka.util.Timeout
import akka.pattern.ask

import com.foram.Main.topicDB
import com.foram.actors.Topic
import com.foram.actors.TopicDB._

import scala.concurrent.duration._
import scala.concurrent.Future

class TopicRoutes {

  import com.foram.JsonFormats._

  implicit val timeout = Timeout(2 seconds)

  val topicRoutes =
    pathPrefix("api" / "topics") {
      get {
        (path(IntNumber) | parameter('id.as[Int])) { id =>
          val topicOptionFuture: Future[Option[Topic]] = (topicDB ? GetTopic(id)).mapTo[Option[Topic]]
          complete(topicOptionFuture)
        } ~
          pathEndOrSingleSlash {
            complete((topicDB ? GetAllTopics).mapTo[List[Topic]])
          }
      } ~
        post {
          entity(as[Topic]) { topic =>
            complete((topicDB ? AddTopic(topic)).map(_ => StatusCodes.OK))
          }
        } ~
        put {
          (path(IntNumber) | parameter('id.as[Int])) { id =>
            entity(as[Topic]) { topic =>
              complete((topicDB ? UpdateTopic(id, topic)).map(_ => StatusCodes.OK))
            }
          }
        } ~
        delete {
          entity(as[Topic]) { topic =>
            complete((topicDB ? RemoveTopic(topic)).map(_ => StatusCodes.OK))
          }
        }
    }
}
