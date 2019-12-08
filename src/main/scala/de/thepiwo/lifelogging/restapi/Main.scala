package de.thepiwo.lifelogging.restapi


import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import de.thepiwo.lifelogging.restapi.http.HttpService
import de.thepiwo.lifelogging.restapi.services.{AuthService, LoggingService, UsersService}
import de.thepiwo.lifelogging.restapi.utils.{Config, DatabaseService, FlywayService}

import scala.concurrent.ExecutionContext

object Main extends App with Config {
  implicit val actorSystem: ActorSystem = ActorSystem("MainSystem")
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val log: LoggingAdapter = Logging(actorSystem, getClass)

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  flywayService.migrateDatabaseSchema

  val databaseService = new DatabaseService(jdbcUrl, dbUser, dbPassword)

  val usersService = new UsersService(databaseService)
  val loggingService = new LoggingService(databaseService)
  val authService = new AuthService(databaseService)(usersService)

  val httpService = new HttpService(usersService, authService, loggingService)

  val schedulerActions = new SchedulerActions(usersService, loggingService)

  Http().bindAndHandle(httpService.routes, httpHost, httpPort)
  log.info(s"Listening on $httpHost:$httpPort")
}




