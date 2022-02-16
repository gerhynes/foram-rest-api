name := "foram-rest-api"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.6.8"
val akkaHttpVersion = "10.2.8"
val scalaTestVersion = "3.0.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  // akka streams
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  // akka http
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  // testing
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion,
  // JWT
  "com.pauldijou" %% "jwt-spray-json" % "2.1.0"
)
