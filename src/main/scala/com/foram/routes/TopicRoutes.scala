package com.foram.routes

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.foram.actors.PostActor._
import com.foram.actors.TopicActor._
import com.foram.auth.Auth.authenticated
import com.foram.models.{Message, Post, Topic, TopicWithChildren}
import spray.json.DefaultJsonProtocol._

import java.util.UUID
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class TopicRoutes(topicActor: ActorRef, postActor: ActorRef) {

  import com.foram.json.JsonFormats._

  implicit val timeout = Timeout(5 seconds)

  val routes =
    pathPrefix("api" / "topics") {
      get {
        path("latest") {
          complete((topicActor ? GetLatestTopics).mapTo[List[Topic]])
        } ~
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
        authenticated { claims => {
          post {
            entity(as[TopicWithChildren]) { newTopic =>
              onComplete((topicActor ? CreateTopic(newTopic)).mapTo[Topic]) {
                case Success(createdTopic) => complete(StatusCodes.Created, createdTopic)
                case Failure(ex) => complete(StatusCodes.InternalServerError)
              }
            }
          } ~
            put {
              path(Segment) { id =>
                val uuid = UUID.fromString(id)
                entity(as[Topic]) { topic =>
                  onComplete((topicActor ? UpdateTopic(uuid, topic)).mapTo[Message]) {
                    case Success(message) => complete(StatusCodes.OK, message)
                    case Failure(ex) => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            } ~
            delete {
              path(Segment) { id =>
                val uuid = UUID.fromString(id)
                onComplete((topicActor ? DeleteTopic(uuid)).mapTo[Message]) {
                  case Success(message) => complete(StatusCodes.OK, message)
                  case Failure(ex) => complete(StatusCodes.InternalServerError)
                }
              }
            }
        }
        }
    }
}

