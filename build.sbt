scalaVersion := "2.13.8"
scalacOptions ++= Seq("-Xfatal-warnings", "-deprecation", "-feature", "-language:postfixOps")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= {
  val akkaVersion = "2.6.19"
  val akkaHttpVersion = "10.2.9"
  val flywayVersion = "8.5.12"
  val hikariCpVersion = "5.0.1"
  val postgresVersion = "42.3.6"
  val scalaBcryptVersion = "4.3.0"
  val scalaTestVersion = "3.2.12"
  val slickVersion = "3.3.3"
  val slickPGVersion = "0.20.3"
  val sprayVersion = "1.3.6"

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

    "io.spray" %% "spray-json" % sprayVersion,

    "com.github.tminglei" %% "slick-pg" % slickPGVersion,
    "com.github.tminglei" %% "slick-pg_spray-json" % slickPGVersion,

    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test"
  ).map(_.exclude("org.slf4j", "*"))
}

libraryDependencies ++= {
  val logBackVersion = "1.2.11"
  val scalaLoggingVersion = "3.9.5"

  Seq(
    "ch.qos.logback" % "logback-classic" % logBackVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
  )
}

lazy val root = (project in file(".")).
  enablePlugins(AssemblyPlugin).
  settings(
    name := "open-lifelogging",
    organization := "de.thepiwo",
    version := "0.0.3",
    scalaVersion := scalaVersion.value
  )

Test / test := {}