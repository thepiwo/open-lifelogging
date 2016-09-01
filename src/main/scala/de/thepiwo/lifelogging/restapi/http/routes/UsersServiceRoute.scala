package de.thepiwo.lifelogging.restapi.http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.PathMatchers.IntNumber
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

  val route = pathPrefix("users") {
    pathEndOrSingleSlash {
      get {
        onComplete(getUsers()) {
          case Success(users) => complete(OK -> users.toJson)
          case Failure(e) => handleFailure(e)
        }
      }
    } ~
      pathPrefix("me") {
        pathEndOrSingleSlash {
          authenticate { loggedUser =>
            get {
              complete(OK -> loggedUser.toJson)
            } ~
              post {
                entity(as[UserEntityUpdate]) { userUpdate =>
                  onComplete(updateUser(loggedUser.id.get, userUpdate)) {
                    case Success(user) => complete(OK -> user.toJson)
                    case Failure(e) => handleFailure(e)
                  }
                }
              }
          }
        }
      } ~
      pathPrefix(IntNumber) { id =>
        pathEndOrSingleSlash {
          get {
            onComplete(getUserById(id)) {
              case Success(user) => complete(OK -> user.toJson)
              case Failure(e) => handleFailure(e)
            }
          } ~
            post {
              entity(as[UserEntityUpdate]) { userUpdate =>
                onComplete(updateUser(id, userUpdate)) {
                  case Success(user) => complete(OK -> user.toJson)
                  case Failure(e) => handleFailure(e)
                }
              }
            } ~
            delete {
              onComplete(deleteUser(id)) {
                case Success(user) => complete(NoContent -> "deleted")
                case Failure(e) => handleFailure(e)
              }
            }
        }
      }
  }

}
