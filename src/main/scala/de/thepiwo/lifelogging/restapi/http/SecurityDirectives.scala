package de.thepiwo.lifelogging.restapi.http

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.{BasicDirectives, FutureDirectives, HeaderDirectives, RouteDirectives}
import de.thepiwo.lifelogging.restapi.models.UserEntity
import de.thepiwo.lifelogging.restapi.services.AuthService
import de.thepiwo.lifelogging.restapi.utils.UnauthorizedException

trait SecurityDirectives {

  import BasicDirectives._
  import FutureDirectives._
  import HeaderDirectives._
  import RouteDirectives._

  def authenticate: Directive1[UserEntity] = {
    headerValueByName("Token").flatMap { token =>
      onSuccess(authService.authenticate(token)).flatMap {
        case Some(user) => provide(user)
        case None => complete((Unauthorized, "token not valid"))
      }
    }
  }

  def handleFailure(e: Throwable): Route = e match {
    case e: UnauthorizedException => complete((Unauthorized, e.getMessage))
    case _ => e.printStackTrace()
      complete((ServiceUnavailable, e.getMessage))
  }

  protected val authService: AuthService

}
