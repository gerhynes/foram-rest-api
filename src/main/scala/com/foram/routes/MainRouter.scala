package com.foram.routes

import akka.http.scaladsl.server.Directives._

object MainRouter {
  val routes = CategoryRoutes.routes ~ UserRoutes.routes ~ TopicRoutes.routes ~ PostRoutes.routes
}