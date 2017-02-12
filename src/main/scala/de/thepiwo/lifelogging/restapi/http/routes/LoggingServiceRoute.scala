package de.thepiwo.lifelogging.restapi.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.thepiwo.lifelogging.restapi.http.SecurityDirectives
import de.thepiwo.lifelogging.restapi.models.LogEntityInsert
import de.thepiwo.lifelogging.restapi.services.{AuthService, LoggingService}
import de.thepiwo.lifelogging.restapi.utils.Helper.localDate
import de.thepiwo.lifelogging.restapi.utils.JsonProtocol
import spray.json._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


class LoggingServiceRoute(val authService: AuthService, loggingService: LoggingService)
                         (implicit executionContext: ExecutionContext) extends JsonProtocol with SecurityDirectives {

  import loggingService._

  val route: Route = pathPrefix("logs") {
    authenticate { loggedUser =>
      parameter("date".?) { dateString =>
        pathEndOrSingleSlash {
          get {
            onComplete(getLogs(loggedUser, dateString.map(localDate))) {
              case Success(logs) => complete(OK -> logs.toJson)
              case Failure(e) => handleFailure(e)
            }
          }
        } ~
          path("key" / Remaining) { logKey =>
            pathEndOrSingleSlash {
              get {
                onComplete(getLogs(loggedUser, logKey, dateString.map(localDate))) {
                  case Success(logs) => complete(OK -> logs.toJson)
                  case Failure(e) => handleFailure(e)
                }

              } ~
                post {
                  entity(as[LogEntityInsert]) { logEntityInsert =>
                    onComplete(createLogItem(loggedUser, logEntityInsert)) {
                      case Success(log) => complete(Created -> log.toJson)
                      case Failure(e) => handleFailure(e)
                    }
                  }
                }
            }
          } ~
          path("keys") {
            pathEndOrSingleSlash {
              get {
                onComplete(getLogKeys(loggedUser)) {
                  case Success(keys) => complete(OK -> keys.toJson)
                  case Failure(e) => handleFailure(e)
                }
              }
            }
          } ~
          path("isCurrent") {
            pathEndOrSingleSlash {
              get {
                onComplete(getLastLogOlderTwoHours(loggedUser)) {
                  case Success(keys) if keys > 0 => complete(OK -> keys.toJson)
                  case Success(keys) => complete(NotAcceptable -> keys.toJson)
                  case Failure(e) => handleFailure(e)
                }
              }
            }
          }
      }
    }
  }
}
