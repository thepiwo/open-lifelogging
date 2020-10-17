package de.thepiwo.lifelogging.restapi.http.routes

import java.time.LocalDate

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.ParameterDirectives.parameters
import de.thepiwo.lifelogging.restapi.http.SecurityDirectives
import de.thepiwo.lifelogging.restapi.models._
import de.thepiwo.lifelogging.restapi.services.{AuthService, LoggingService}
import de.thepiwo.lifelogging.restapi.utils.Helper.localDate
import de.thepiwo.lifelogging.restapi.utils.JsonProtocol
import spray.json._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

case class DateOptions(fromDate: LocalDate, toDate: Option[LocalDate])

class LoggingServiceRoute(val authService: AuthService, loggingService: LoggingService)
                         (implicit executionContext: ExecutionContext) extends JsonProtocol with SecurityDirectives {

  import loggingService._

  val route: Route = pathPrefix("logs") {
    authenticate { implicit loggedUser =>
      byDateRoutes ~
        getLatestLogsRoute ~
        getLogKeysRoute ~
        logByIdRoute ~
        areLogsCurrentRoute
    }
  }

  def byDateRoutes(implicit loggedUser: UserEntity): Route =
    parameters("date".?, "toDate".?, "limitType".?) { case (fromDateString, toDateString, limitTypeString) =>
      val fromDateOption: Option[LocalDate] = fromDateString.flatMap(localDate)
      val toDateOption: Option[LocalDate] = toDateString.flatMap(localDate)

      implicit val fetchType: LimitType = LimitType.fromString(limitTypeString, LimitModId)
      implicit val dateOptions: Option[DateOptions] = fromDateOption.map(fromDate => DateOptions(fromDate, toDateOption))

      allLogsRoute ~
        logsByKeyRoute
    }

  def allLogsRoute(implicit loggedUser: UserEntity, dateOptions: Option[DateOptions], limitType: LimitType): Route =
    pathEndOrSingleSlash {
      get {
        onComplete(getLogs(loggedUser, dateOptions, limitType)) {
          case Success(logs) => complete(OK -> logs.toJson)
          case Failure(e) => handleFailure(e)
        }
      }
    }

  def logsByKeyRoute(implicit loggedUser: UserEntity, dateOptions: Option[DateOptions], limitType: LimitType): Route =
    path("key" / Remaining) { logKey =>
      pathEndOrSingleSlash {
        get {
          onComplete(getLogs(loggedUser, logKey, dateOptions, limitType)) {
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

  def getLatestLogsRoute(implicit loggedUser: UserEntity): Route =
    path("latest") {
      parameters("limit".?) { limitString =>
        pathEndOrSingleSlash {
          get {
            onComplete(getLatestLogs(loggedUser, limitString.map(_.toLong))) {
              case Success(logs) => complete(OK -> logs.toJson)
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

  def logByIdRoute(implicit loggedUser: UserEntity): Route =
    pathPrefix(IntNumber) { id =>
      pathEndOrSingleSlash {
        delete {
          onComplete(deleteLogById(loggedUser, id)) {
            case Success(_) => complete(NoContent)
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
