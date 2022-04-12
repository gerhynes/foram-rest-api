package com.foram.routes

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.foram.models.User
import scala.concurrent.duration._

class UserRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  // val userRoutes: Route = new UserRoutes().routes

  import com.foram.JsonFormats._

  implicit val timeout = Timeout(5 seconds)

//  "UserRoutes" should {
//    "return a list of Users for GET requests to the users path" in {
//      Get("/api/users") ~> userRoutes ~> check {
//        responseAs[List[User]] shouldEqual ???
//      }
//    }
//  }
}

