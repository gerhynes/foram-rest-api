package com.foram

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem(Behaviors.empty, "foramSystem")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    val route =
      path("categories") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hello from Foram!</h1>"))
        }
      }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    println(s"Server now online at http://localhost:8080/categories\nPress RETURN to stop server")
    StdIn.readLine() // let server run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
    }
}
