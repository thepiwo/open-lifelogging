package de.thepiwo.lifelogging.restapi.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import de.thepiwo.lifelogging.restapi.http.SecurityDirectives
import de.thepiwo.lifelogging.restapi.models.UserEntity
import de.thepiwo.lifelogging.restapi.services.AuthService
import de.thepiwo.lifelogging.restapi.utils.{JsonProtocol, LoginPassword}
import spray.json._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class AuthServiceRoute(val authService: AuthService)
                      (implicit executionContext: ExecutionContext) extends JsonProtocol with SecurityDirectives {

  import authService._

  val route = pathPrefix("auth") {
    path("signIn") {
      pathEndOrSingleSlash {
        post {
          entity(as[LoginPassword]) { loginPassword =>
            onComplete(signIn(loginPassword.login, loginPassword.password)) {
              case Success(tokenOption) => complete(OK -> tokenOption.toJson)
              case Failure(e) => handleFailure(e)
            }
          }
        }
      }
    } ~
      path("signUp") {
        pathEndOrSingleSlash {
          post {
            entity(as[UserEntity]) { userEntity =>
              onComplete(signUp(userEntity)) {
                case Success(token) => complete(Created -> token.toJson)
                case Failure(e) => handleFailure(e)
              }
            }
          }
        }
      }
  }
}
