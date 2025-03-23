package de.thepiwo.lifelogging.restapi


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{Materializer, SystemMaterializer}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import de.thepiwo.lifelogging.restapi.http.HttpService
import de.thepiwo.lifelogging.restapi.services.{AuthService, ImportService, LoggingService, UsersService}
import de.thepiwo.lifelogging.restapi.utils.{Config, DatabaseService, FlywayService}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object Main
  extends App with Config with LazyLogging {
  implicit val timeout: Timeout = Timeout(10.minutes)
  implicit val actorSystem: ActorSystem = ActorSystem("MainSystem")
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val materializer: Materializer = SystemMaterializer(actorSystem.classicSystem).materializer

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  flywayService.migrateDatabaseSchema

  val databaseService = new DatabaseService(jdbcUrl, dbUser, dbPassword)

  val usersService = new UsersService(databaseService)
  val loggingService = new LoggingService(databaseService)
  val authService = new AuthService(databaseService)(usersService)
  val importService = new ImportService(databaseService, loggingService)

  val httpService = new HttpService(usersService, authService, loggingService, importService)

  val schedulerActions = new SchedulerActions(usersService, loggingService)

  Http().newServerAt(httpHost, httpPort).bind(httpService.routes)
  logger.info(s"Listening on $httpHost:$httpPort")
}




