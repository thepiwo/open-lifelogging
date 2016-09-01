package de.thepiwo.lifelogging.restapi.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import de.thepiwo.lifelogging.restapi.http.SecurityDirectives
import de.thepiwo.lifelogging.restapi.models.LogEntityInsert
import de.thepiwo.lifelogging.restapi.services.{AuthService, LoggingService}
import de.thepiwo.lifelogging.restapi.utils.JsonProtocol
import spray.json._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


class LoggingServiceRoute(val authService: AuthService, loggingService: LoggingService)
                         (implicit executionContext: ExecutionContext) extends JsonProtocol with SecurityDirectives {

  import loggingService._

  val route = pathPrefix("logs") {
    authenticate { loggedUser =>
        pathEndOrSingleSlash {
          get {
            onComplete(getLogs(loggedUser)) {
              case Success(logs) => complete(OK -> logs.toJson)
              case Failure(e) => handleFailure(e)
            }
          }
      } ~
        path("key" / Remaining) { logKey =>
          pathEndOrSingleSlash {
            get {
              onComplete(getLogs(loggedUser, logKey)) {
                case Success(logs) => complete(OK -> logs.toJson)
                case Failure(e) => handleFailure(e)
              }

            } ~
              post {
                entity(as[LogEntityInsert]) { logEntityInsert =>
                  onComplete(createLogItem(loggedUser, logKey, logEntityInsert)) {
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
        }
    }
  }
}
