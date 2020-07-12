package de.thepiwo.lifelogging.restapi.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.thepiwo.lifelogging.restapi.http.SecurityDirectives
import de.thepiwo.lifelogging.restapi.models.UserEntity
import de.thepiwo.lifelogging.restapi.services.{AuthService, ImportService}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class ImportServiceRoute(val authService: AuthService, val importService: ImportService)
                        (implicit executionContext: ExecutionContext) extends SecurityDirectives {

  import importService._

  val route: Route = pathPrefix("import") {
    authenticate { implicit loggedUser =>
      googleRoute
    }
  }

  def googleRoute(implicit loggedUser: UserEntity): Route =
    path("google") {
      pathEndOrSingleSlash {
        post {
          fileUpload("json") { case (_, byteSource) =>
            onComplete(importGoogle(byteSource)) {
              case Success(count) => complete(OK -> count.toString)
              case Failure(e) => handleFailure(e)
            }
          }
        }
      }
    }
}
