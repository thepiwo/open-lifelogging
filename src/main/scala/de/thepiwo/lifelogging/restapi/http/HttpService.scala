package de.thepiwo.lifelogging.restapi.http

import akka.http.scaladsl.server.Directives._
import de.thepiwo.lifelogging.restapi.http.routes.{AuthServiceRoute, UsersServiceRoute}
import de.thepiwo.lifelogging.restapi.services.{AuthService, UsersService}
import de.thepiwo.lifelogging.restapi.utils.CorsSupport

import scala.concurrent.ExecutionContext

class HttpService(usersService: UsersService, authService: AuthService)
                 (implicit executionContext: ExecutionContext) extends CorsSupport {

  val usersRouter = new UsersServiceRoute(authService, usersService)
  val authRouter = new AuthServiceRoute(authService)

  val routes =
    pathPrefix("v1") {
      corsHandler {
        usersRouter.route ~
          authRouter.route
      }
    }

}
