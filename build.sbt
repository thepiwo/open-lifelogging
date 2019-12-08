import sbt.Keys._
import sbt.Resolver

scalaVersion := "2.13.1"
scalacOptions ++= Seq("-Xlint:-missing-interpolator", "-Xfatal-warnings", "-deprecation", "-feature", "-language:implicitConversions", "-language:postfixOps")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= {
  val akkaVersion = "2.6.1"
  val akkaHttpVersion = "10.1.11"
  val flywayVersion = "6.1.0"
  val hikariCpVersion = "3.4.1"
  val logBackVersion = "1.2.3"
  val postgresVersion = "42.2.8"
  val scalaBcryptVersion = "4.1"
  val scalaLoggingVersion = "3.9.2"
  val scalaTestVersion = "3.1.0"
  val slickVersion = "3.3.2"
  val slickPGVersion = "0.18.1"
  val sprayVersion = "1.3.5"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

    "com.github.t3hnar" %% "scala-bcrypt" % scalaBcryptVersion,
    "com.typesafe.slick" %% "slick" % slickVersion,
    "org.postgresql" % "postgresql" % postgresVersion,
    "org.flywaydb" % "flyway-core" % flywayVersion,

    "com.zaxxer" % "HikariCP" % hikariCpVersion,

    "ch.qos.logback" % "logback-classic" % logBackVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,

    "io.spray" %% "spray-json" % sprayVersion,

    "com.github.tminglei" %% "slick-pg" % slickPGVersion,
    "com.github.tminglei" %% "slick-pg_spray-json" % slickPGVersion,

    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test"
  )
}

Revolver.settings

lazy val root = (project in file(".")).
  enablePlugins(AssemblyPlugin).
  settings(
    name := "open-lifelogging",
    organization := "de.thepiwo",
    version := "0.0.3",
    scalaVersion := "2.13.1"
  )

test in assembly := {}
