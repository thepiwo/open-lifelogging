package de.thepiwo.lifelogging.restapi.http.routes

import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import de.thepiwo.lifelogging.restapi.http.SecurityDirectives
import de.thepiwo.lifelogging.restapi.models.LogEntityInsert
import de.thepiwo.lifelogging.restapi.services.{AuthService, LoggingService}
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

class LoggingServiceRoute(val authService: AuthService, loggingService: LoggingService)
                         (implicit executionContext: ExecutionContext) extends CirceSupport with SecurityDirectives {

  import loggingService._

  val route = pathPrefix("logs") {
    authenticate { loggedUser =>
      pathEndOrSingleSlash {
        get {
          complete(getLogs(loggedUser).map(_.asJson))
        }
      } ~
        path("key" / Remaining) { logKey =>
          pathEndOrSingleSlash {
            get {
              complete(getLogs(loggedUser, logKey).map(_.asJson))
            } ~
              post {
                entity(as[LogEntityInsert]) { logEntityInsert =>
                  complete(createLogItem(loggedUser, logKey, logEntityInsert).map(_.asJson))
                }
              }
          }
        } ~
        path("keys") {
          pathEndOrSingleSlash {
            get {
              complete(getLogKeys().map(_.asJson))
            }
          }
        }
    }
  }
}
