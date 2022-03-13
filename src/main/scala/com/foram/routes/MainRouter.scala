package com.foram.routes

import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

object MainRouter {

  val categoryRoutes = CategoryRoutes.routes
  val userRoutes = UserRoutes.routes
  val topicRoutes = TopicRoutes.routes
  val postRoutes = PostRoutes.routes

//  val routes = CategoryRoutes.routes ~ UserRoutes.routes ~ TopicRoutes.routes ~ PostRoutes.routes

  val routes = cors() {
    concat(categoryRoutes, userRoutes, topicRoutes, postRoutes)
  }
}