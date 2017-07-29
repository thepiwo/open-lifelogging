import sbt.Keys._
import sbt.Resolver

scalaVersion := "2.12.3"
scalacOptions ++= Seq("-Xlint:-missing-interpolator", "-Xfatal-warnings", "-deprecation", "-feature", "-language:implicitConversions", "-language:postfixOps", "-Xmax-classfile-name", "240")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= {
  val akkaVersion = "10.0.9"
  val flywayVersion = "4.2.0"
  val hikariCpVersion = "2.6.3"
  val logBackVersion = "1.2.3"
  val postgresVersion = "42.1.3"
  val scalaBcryptVersion = "3.1"
  val scalaLoggingVersion = "3.7.2"
  val scalaMacrosVersion = "2.1.1"
  val scalaTestVersion = "3.0.3"
  val slickVersion = "3.2.1"
  val slickPGVersion = "0.15.2"
  val sprayVersion = "1.3.3"

  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaVersion,

    "com.github.t3hnar" %% "scala-bcrypt" % scalaBcryptVersion,
    "com.typesafe.slick" %% "slick" % slickVersion,
    "org.postgresql" % "postgresql" % postgresVersion,
    "org.flywaydb" % "flyway-core" % flywayVersion,

    "org.scalamacros" % "paradise_2.12.3" % scalaMacrosVersion,
    "com.zaxxer" % "HikariCP" % hikariCpVersion,

    "ch.qos.logback" % "logback-classic" % logBackVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,

    "io.spray" %% "spray-json" % sprayVersion,

    "com.github.tminglei" %% "slick-pg" % slickPGVersion,
    "com.github.tminglei" %% "slick-pg_spray-json" % slickPGVersion,

    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % "test"
  )
}

Revolver.settings

lazy val root = (project in file(".")).
  enablePlugins(AssemblyPlugin).
  settings(
    name := "open-lifelogging",
    organization := "de.thepiwo",
    version := "0.0.2",
    scalaVersion := "2.12.2"
  )

test in assembly := {}
