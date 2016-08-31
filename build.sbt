import sbt.Keys._

libraryDependencies ++= {
  val akkaVersion = "2.4.9"
  val scalaTestVersion = "3.0.0"
  val slickVersion = "3.2.0-M1"
  val circeVersion = "0.4.1"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion,
    "de.heikoseeberger" %% "akka-http-circe" % "1.9.0",

    "com.github.t3hnar" %% "scala-bcrypt" % "2.6",
    "org.apache.commons" % "commons-lang3" % "3.4",
    "com.typesafe.slick" %% "slick" % slickVersion,
    "org.postgresql" % "postgresql" % "9.4-1206-jdbc41",
    "org.flywaydb" % "flyway-core" % "3.2.1",

    "com.zaxxer" % "HikariCP" % "2.4.7",
    "org.slf4j" % "slf4j-nop" % "1.7.21",

    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,

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