package com.foram.routes

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, ExceptionHandler, RejectionHandler, Route}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.foram.models.Message

class MainRouter(categoryActor: ActorRef, userActor: ActorRef, topicActor: ActorRef, postActor: ActorRef) {
  import com.foram.JsonFormats._

  // Instantiate routes
  val authRoutes: Route = new AuthRoutes(userActor).routes
  val categoryRoutes: Route = new CategoryRoutes(categoryActor, topicActor).routes
  val userRoutes: Route = new UserRoutes(userActor, topicActor, postActor).routes
  val topicRoutes: Route = new TopicRoutes(topicActor, postActor).routes
  val postRoutes: Route = new PostRoutes(postActor).routes

  // Custom rejection handler
  val rejectionHandler: RejectionHandler = corsRejectionHandler.withFallback(RejectionHandler.default)

  // Custom exception handler
  val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: NoSuchElementException =>
      complete(StatusCodes.NotFound, Message("Cannot find resource"))
    case e: ClassCastException =>
      complete(StatusCodes.NotFound, Message("Incorrect resource returned"))
    case e: IllegalArgumentException =>
      complete(StatusCodes.BadRequest, Message("Illegal argument passed"))
    case e: RuntimeException =>
      complete(StatusCodes.InternalServerError, Message(e.getMessage))
  }

  val handleErrors: Directive[Unit] = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)

  val routes: Route = {
    cors() {
      handleErrors {
        concat(authRoutes, categoryRoutes, userRoutes, topicRoutes, postRoutes)
      }
    }
  }
}