import sbt.Keys._
import sbt.Resolver

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= {
  val akkaVersion = "2.4.9"
  val scalaTestVersion = "3.0.0"
  val slickVersion = "3.1.1"
  val slickPGVersion = "0.14.3"
  val sprayVersion = "1.3.2"
  val logBackVersion = "1.1.7"
  val scalaLoggingVersion = "3.5.0"


  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion,

    "com.github.t3hnar" %% "scala-bcrypt" % "2.6",
    "com.typesafe.slick" %% "slick" % slickVersion,
    "org.postgresql" % "postgresql" % "9.4-1206-jdbc41",
    "org.flywaydb" % "flyway-core" % "3.2.1",

    "org.scalamacros" % "paradise_2.10.6" % "2.1.0",
    "com.zaxxer" % "HikariCP" % "2.4.7",

    "ch.qos.logback" % "logback-classic" % logBackVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,

    "io.spray" %% "spray-json" % sprayVersion,
    "io.spray" %% "spray-client" % sprayVersion,

    "com.github.tminglei" %% "slick-pg" % slickPGVersion,
    "com.github.tminglei" %% "slick-pg_spray-json" % slickPGVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.9",

    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % "test",
    "ru.yandex.qatools.embed" % "postgresql-embedded" % "1.15" % "test"
  )
}

Revolver.settings

lazy val root = (project in file(".")).
  enablePlugins(JavaAppPackaging).
  enablePlugins(DockerPlugin).
  settings(
    name := "open-lifelogging",
    organization := "de.thepiwo",
    version := "0.0.1",
    scalaVersion := "2.11.8"
  )

dockerExposedPorts := Seq(9000)
dockerEntrypoint := Seq("bin/%s" format executableScriptName.value, "-Dconfig.resource=docker.conf")

