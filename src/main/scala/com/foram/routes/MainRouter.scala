package com.foram.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, ExceptionHandler, RejectionHandler, Route}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

object MainRouter {

  val authRoutes: Route = AuthRoutes.routes
  val categoryRoutes: Route = CategoryRoutes.routes
  val userRoutes: Route = UserRoutes.routes
  val topicRoutes: Route = TopicRoutes.routes
  val postRoutes: Route = PostRoutes.routes

  val rejectionHandler: RejectionHandler = corsRejectionHandler.withFallback(RejectionHandler.default)

  val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: NoSuchElementException =>
      complete(StatusCodes.NotFound, "Cannot find resource")
    case e: ClassCastException =>
      complete(StatusCodes.NotFound, "Incorrect resource returned")
    case e: RuntimeException =>
      complete(StatusCodes.NotFound, e.getMessage)
    case e: IllegalArgumentException =>
      complete(StatusCodes.BadRequest, "Illegal argument passed")
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