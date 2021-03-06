package de.thepiwo.lifelogging.restapi.http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.PathMatchers.IntNumber
import akka.http.scaladsl.server.Route
import de.thepiwo.lifelogging.restapi.http.SecurityDirectives
import de.thepiwo.lifelogging.restapi.models.UserEntityUpdate
import de.thepiwo.lifelogging.restapi.services.{AuthService, UsersService}
import de.thepiwo.lifelogging.restapi.utils.JsonProtocol
import spray.json._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class UsersServiceRoute(val authService: AuthService, usersService: UsersService)
                       (implicit executionContext: ExecutionContext) extends JsonProtocol with SecurityDirectives {

  import usersService._

  val route: Route = pathPrefix("users") {
    getUsersRoute ~
      userMeRoute ~
      getUserByIdRoute
  }

  def getUsersRoute: Route =
    pathEndOrSingleSlash {
      get {
        onComplete(getUsers()) {
          case Success(users) => complete(OK -> users.toJson)
          case Failure(e) => handleFailure(e)
        }
      }
    }

  def userMeRoute: Route =
    pathPrefix("me") {
      pathEndOrSingleSlash {
        authenticate { loggedUser =>
          get {
            complete(OK -> loggedUser.toJson)
          } ~
            post {
              entity(as[UserEntityUpdate]) { userUpdate =>
                onComplete(updateUser(loggedUser, userUpdate)) {
                  case Success(user) => complete(OK -> user.toJson)
                  case Failure(e) => handleFailure(e)
                }
              }
            } ~
            delete {
              onComplete(deleteUser(loggedUser)) {
                case Success(_) => complete(OK -> "deleted")
                case Failure(e) => handleFailure(e)
              }
            }
        }
      }
    }

  def getUserByIdRoute: Route =
    pathPrefix(IntNumber) { id =>
      pathEndOrSingleSlash {
        get {
          onComplete(getUserById(id)) {
            case Success(user) => complete(OK -> user.toJson)
            case Failure(e) => handleFailure(e)
          }
        }
      }
    }

}
