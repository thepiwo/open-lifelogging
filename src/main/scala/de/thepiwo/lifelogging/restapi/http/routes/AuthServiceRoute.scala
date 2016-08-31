package de.thepiwo.lifelogging.restapi.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import de.thepiwo.lifelogging.restapi.http.SecurityDirectives
import de.thepiwo.lifelogging.restapi.models.UserEntity
import de.thepiwo.lifelogging.restapi.services.AuthService
import de.thepiwo.lifelogging.restapi.utils.LoginPassword
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class AuthServiceRoute(val authService: AuthService)
                      (implicit executionContext: ExecutionContext) extends CirceSupport with SecurityDirectives {

  import authService._

  val route = pathPrefix("auth") {
    path("signIn") {
      pathEndOrSingleSlash {
        post {
          entity(as[LoginPassword]) { loginPassword =>
            onComplete(signIn(loginPassword.login, loginPassword.password)) {
              case Success(tokenOption) => complete(tokenOption.asJson)
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
              complete(Created -> signUp(userEntity).map(_.asJson))
            }
          }
        }
      }
  }
}
