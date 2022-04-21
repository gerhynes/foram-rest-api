package com.foram.dao

import com.foram.models.User
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Query, TableQuery}

import java.time.OffsetDateTime
import java.util.UUID.randomUUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UsersDaoSpec extends AnyWordSpec with Matchers with ScalaFutures with MockFactory {
  def mockDB = mock[PostgresProfile.backend.Database]

  val sampleUser: User = User(randomUUID(), "Quincy Lars", "quincy", "qlars@example.com", "password123", "admin", OffsetDateTime.now(), OffsetDateTime.now())

  "UsersDao" should {
//    "return a Seq of Users from findAll" in {
//      val usersDao = new UsersDao(mockDB)
//
//      val usersQuery = usersDao.users.sortBy(_.createdAt.asc).result
//
//      (mockDB.run _).expects(usersQuery).returning(Future(Seq(sampleUser)))
//
//      val usersFuture = usersDao.findAll
//
//      usersFuture.futureValue shouldBe Seq(sampleUser)
//    }
  }
}
