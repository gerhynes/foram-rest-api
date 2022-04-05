name := "foram-rest-api"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.6.8"
val akkaHttpVersion = "10.2.7"
val scalaTestVersion = "3.1.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  // akka streams
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  // akka http
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  // testing
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion,
  "org.scalamock" %% "scalamock" % "5.1.0",
  // Slick and Postgres
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "org.postgresql" % "postgresql" % "42.3.3",
  // CORS
  "ch.megard" %% "akka-http-cors" % "1.1.3",
  "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0"
)
