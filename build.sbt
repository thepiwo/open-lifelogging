import sbt.Keys._
import sbt.Resolver

scalaVersion := "2.11.8"
scalacOptions ++= Seq("-Xlint:-missing-interpolator", "-Xfatal-warnings", "-deprecation", "-feature", "-language:implicitConversions", "-language:postfixOps", "-Xmax-classfile-name", "240")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= {
  val akkaVersion = "10.0.3"
  val flywayVersion = "4.0.3"
  val hikariCpVersion = "2.4.7"
  val logBackVersion = "1.1.7"
  val postgresVersion = "9.4-1206-jdbc41"
  val scalaBcryptVersion = "2.6"
  val scalaLoggingVersion = "3.5.0"
  val scalaMacrosVersion = "2.1.0"
  val scalaTestVersion = "3.0.1"
  val slickVersion = "3.1.1"
  val slickPGVersion = "0.14.5"
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

    "org.scalamacros" % "paradise_2.10.6" % scalaMacrosVersion,
    "com.zaxxer" % "HikariCP" % hikariCpVersion,

    "ch.qos.logback" % "logback-classic" % logBackVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,

    "io.spray" %% "spray-json" % sprayVersion,
    "io.spray" %% "spray-client" % sprayVersion,

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
    version := "0.0.1",
    scalaVersion := "2.11.8"
  )

test in assembly := {}
