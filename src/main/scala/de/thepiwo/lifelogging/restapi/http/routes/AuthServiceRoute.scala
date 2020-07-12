package de.thepiwo.lifelogging.restapi.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.thepiwo.lifelogging.restapi.http.SecurityDirectives
import de.thepiwo.lifelogging.restapi.models.SignUpUser
import de.thepiwo.lifelogging.restapi.services.AuthService
import de.thepiwo.lifelogging.restapi.utils.{JsonProtocol, LoginPassword}
import spray.json._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class AuthServiceRoute(val authService: AuthService)
                      (implicit executionContext: ExecutionContext) extends JsonProtocol with SecurityDirectives {

  import authService._

  val route: Route = pathPrefix("auth") {
    signInRoute ~
      signUpRoute
  }

  def signInRoute: Route =
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
    }

  def signUpRoute: Route =
    path("signUp") {
      pathEndOrSingleSlash {
        post {
          entity(as[SignUpUser]) { signUpUser =>
            onComplete(signUp(signUpUser)) {
              case Success(token) => complete(Created -> token.toJson)
              case Failure(e) => handleFailure(e)
            }
          }
        }
      }
    }
}
