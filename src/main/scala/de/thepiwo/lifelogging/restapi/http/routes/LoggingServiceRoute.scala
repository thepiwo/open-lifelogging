package de.thepiwo.lifelogging.restapi.http.routes

import java.time.LocalDate

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.thepiwo.lifelogging.restapi.http.SecurityDirectives
import de.thepiwo.lifelogging.restapi.models.{LogEntityInsert, UserEntity}
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
    authenticate { implicit loggedUser =>
      parameter("date".?) { dateString =>
        implicit val dateOption: Option[LocalDate] = dateString.flatMap(localDate)

        allLogsRoute ~
          logsByKeyRoute ~
          getLogKeysRoute ~
          areLogsCurrentRoute
      }
    }
  }

  def allLogsRoute(implicit loggedUser: UserEntity, dateOption: Option[LocalDate]): Route =
    pathEndOrSingleSlash {
      get {
        onComplete(getLogs(loggedUser, dateOption)) {
          case Success(logs) => complete(OK -> logs.toJson)
          case Failure(e) => handleFailure(e)
        }
      }
    }

  def logsByKeyRoute(implicit loggedUser: UserEntity, dateOption: Option[LocalDate]): Route =
    path("key" / Remaining) { logKey =>
      pathEndOrSingleSlash {
        get {
          onComplete(getLogs(loggedUser, logKey, dateOption)) {
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
    }

  def getLogKeysRoute(implicit loggedUser: UserEntity): Route =
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

  def areLogsCurrentRoute(implicit loggedUser: UserEntity): Route =
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
