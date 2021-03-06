package com.foram

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.foram.actors._
import com.foram.daos.{CategoriesDao, PostsDao, TopicsDao, UsersDao}
import com.foram.routes._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import slick.jdbc.PostgresProfile.api._

object Main extends App {
  // Set up actor system
  implicit val system = ActorSystem("foramSystem")
  implicit val materializer = ActorMaterializer()

  // Provide database config
  def db = Database.forConfig("postgresDB")

  // Set up actors
  val categoryActor = system.actorOf(Props (new CategoryActor(new CategoriesDao(db),
    new TopicsDao(db), new PostsDao(db))), "categoryActor")
  val userActor = system.actorOf(Props (new UserActor(new UsersDao(db))), "userActor")
  val topicActor = system.actorOf(Props (new TopicActor(new TopicsDao(db),
    new PostsDao(db))), "topicActor")
  val postActor = system.actorOf(Props (new PostActor(new PostsDao(db))), "postActor")

  // Get all routes
  val routes = new MainRouter(categoryActor, userActor, topicActor, postActor).routes

  // Bind server
  val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)
  bindingFuture.onComplete {
    case Success(bound) => println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) => Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }
}