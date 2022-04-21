//package com.foram.dao
//
//import com.foram.auth.Auth
//import com.foram.models.{CategoriesTable, Category, Post, PostsTable, Topic, TopicsTable, User, UsersTable}
//import org.scalatest._
//import org.scalatest.concurrent.ScalaFutures
//import org.scalatest.time.{Seconds, Span}
//import org.scalatest.{BeforeAndAfter, funsuite}
//
//import scala.concurrent.Await
////import slick.jdbc.H2Profile.api._
//import slick.jdbc.meta._
//import slick.jdbc.PostgresProfile
//import slick.jdbc.PostgresProfile.api._
//
//import java.time.OffsetDateTime
//import java.util.UUID
//import scala.concurrent.ExecutionContext.Implicits.global
//
//class PostsDaoSpec extends funsuite.AnyFunSuite with BeforeAndAfter with ScalaFutures {
//  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
//
//  val users = TableQuery[UsersTable]
//  val categories = TableQuery[CategoriesTable]
//  val topics = TableQuery[TopicsTable]
//  val posts = TableQuery[PostsTable]
//
//  val sampleCategory: Category = Category(UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", "javascript", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Ask questions and share tips for JavaScript, React, Node - anything to do with the JavaScript ecosystem.", OffsetDateTime.now(), OffsetDateTime.now())
//  val sampleTopic: Topic = Topic(UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "I don't understand promises in JavaScript. Help!", "i-dont-understand-promises-in-javascript-help", UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "quince", UUID.fromString("355e95e6-6f03-499a-a577-6c2f6e088759"), "JavaScript", OffsetDateTime.now(), OffsetDateTime.now())
//  val samplePost: Post = Post(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Lorem ipsum dolor sit amet, consectetur adipiscing enim", OffsetDateTime.now(), OffsetDateTime.now())
//  val sampleUser: User = User(UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", "quincy", "qlars@example.com", "password123", "admin", OffsetDateTime.now(), OffsetDateTime.now())
//
//  val sampleNewPost: Post = Post(UUID.fromString("ab93388b-2941-4d84-bca6-f6f5e9b9349d"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 2, "Et leo duis ut diam quam nulla porttitor.", OffsetDateTime.now(), OffsetDateTime.now())
//  val sampleUpdatedPost: Post = Post(UUID.fromString("e5760f56-4bf0-4b56-bf6e-2f8c9aee8707"), UUID.fromString("33de6e57-c57c-4451-82b9-b73ae248c672"), "Quincy Lars", UUID.fromString("52e787b3-adb3-44ee-9c64-d19247ffd946"), "i-dont-understand-promises-in-javascript-help", 1, "Turpis in eu mi bibendum neque egestas congue quisque egestas.", OffsetDateTime.now(), OffsetDateTime.now())
//
//
//  val db: PostgresProfile.backend.Database = Database.forConfig("testDB")
//
//  def createSchemas() =
//    db.run((users.schema ++ categories.schema ++ topics.schema ++ posts.schema).createIfNotExists)
//
//  def dropSchemas() = {
//    db.run((users.schema ++ categories.schema ++ topics.schema ++ posts.schema).dropIfExists)
//  }
//
//  def seedDB() = {
//    val setupAction = DBIO.seq(
//      users += sampleUser,
//      categories += sampleCategory,
//      topics += sampleTopic,
//      posts += samplePost
//    )
//    db.run(setupAction)
//  }
//
//  before {
//    dropSchemas().flatMap(_ => createSchemas().flatMap(_ => seedDB())).futureValue
//  }
//
////  test("Creating the Schema works") {
////    createSchemas()
////
////    val tables = db.run(MTable.getTables).futureValue
////
////    assert(tables.count(_.name.name.equalsIgnoreCase("users")) == 1)
////    assert(tables.count(_.name.name.equalsIgnoreCase("categories")) == 1)
////    assert(tables.count(_.name.name.equalsIgnoreCase("topics")) == 1)
////    assert(tables.count(_.name.name.equalsIgnoreCase("posts")) == 1)
////  }
//
////  test("Seeding the DB works") {
////    createSchemas()
////
////    seedDB()
////
////    val category = db.run(categories.filter(_.id === sampleCategory.id).result.head).futureValue
////    val topic = db.run(topics.filter(_.id === sampleTopic.id).result.head).futureValue
////    val post = db.run(posts.filter(_.id === samplePost.id).result.head).futureValue
////    val user = db.run(users.filter(_.id === sampleUser.id).result.head).futureValue
////
////    assert(category == sampleCategory)
////    assert(topic == sampleTopic)
////    assert(post == samplePost)
////    assert(user == sampleUser)
////  }
//
//  //  test("Dropping the schemas works") {
//  //    createSchemas()
//  //  }
//
//  test("Inserting a Post works") {
//    //    createSchemas()
//
//    val postsDao = new PostsDao(db)
//
//    val postId = postsDao.create(sampleNewPost).futureValue
//
//    assert(postId == sampleNewPost.id)
//  }
//
//  test("Updating a Post works") {
//    val postsDao = new PostsDao(db)
//
//    val rowsUpdated = postsDao.update(samplePost.id, sampleUpdatedPost).futureValue
//
//    val updatedPost = db.run(posts.filter(_.id === sampleUpdatedPost.id).result.head).futureValue
//
//    assert(rowsUpdated == 1)
//    assert(updatedPost == sampleUpdatedPost)
//  }
//
//  test("Deleting a Post works") {
//    val postsDao = new PostsDao(db)
//
//    val rowsDeleted = postsDao.delete(samplePost.id).futureValue
//
//    val res = db.run(posts.sortBy(_.createdAt.asc).result).futureValue
//
//    assert(rowsDeleted == 1)
////    assert(res contains samplePost == false)
//  }
//
//  test("Getting all posts works") {
//    val postsDao = new PostsDao(db)
//
//    val postList = db.run(posts.sortBy(_.createdAt.asc).result).futureValue.toList
//
//    assert(postList == List(samplePost))
//  }
//
//  after {
//    db.close
//  }
//
//}
