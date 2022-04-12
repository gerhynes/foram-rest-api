package com.foram.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, ExceptionHandler, RejectionHandler, Route}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

class MainRouter(categoryActor: ActorRef, userActor: ActorRef, topicActor: ActorRef, postActor: ActorRef) {

  val authRoutes: Route = new AuthRoutes(userActor).routes
  val categoryRoutes: Route = new CategoryRoutes(categoryActor, topicActor).routes
  val userRoutes: Route = new UserRoutes(userActor, topicActor, postActor).routes
  val topicRoutes: Route = new TopicRoutes(topicActor, postActor).routes
  val postRoutes: Route = new PostRoutes(postActor).routes

  val rejectionHandler: RejectionHandler = corsRejectionHandler.withFallback(RejectionHandler.default)

  val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: NoSuchElementException =>
      complete(StatusCodes.NotFound, "Cannot find resource")
    case e: ClassCastException =>
      complete(StatusCodes.NotFound, "Incorrect resource returned")
    case e: IllegalArgumentException =>
      complete(StatusCodes.BadRequest, "Illegal argument passed")
    case e: RuntimeException =>
      complete(StatusCodes.NotFound, e.getMessage)
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