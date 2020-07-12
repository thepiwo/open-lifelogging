package de.thepiwo.lifelogging.restapi.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.thepiwo.lifelogging.restapi.http.routes.{AuthServiceRoute, ImportServiceRoute, LoggingServiceRoute, UsersServiceRoute}
import de.thepiwo.lifelogging.restapi.services.{AuthService, ImportService, LoggingService, UsersService}
import de.thepiwo.lifelogging.restapi.utils.CorsSupport

import scala.concurrent.ExecutionContext

class HttpService(usersService: UsersService, authService: AuthService, logsService: LoggingService, importService: ImportService)
                 (implicit executionContext: ExecutionContext) extends CorsSupport {

  val usersRouter = new UsersServiceRoute(authService, usersService)
  val authRouter = new AuthServiceRoute(authService)
  val loggingRouter = new LoggingServiceRoute(authService, logsService)
  val importRouter = new ImportServiceRoute(authService, importService)

  val routes: Route =
    pathPrefix("v1") {
      corsHandler {
        usersRouter.route ~
          authRouter.route ~
          loggingRouter.route ~
          importRouter.route
      }
    }

}
