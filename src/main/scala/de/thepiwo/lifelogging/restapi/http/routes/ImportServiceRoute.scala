package de.thepiwo.lifelogging.restapi.http.routes

import java.io.File

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FileInfo
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
      googleRoute ~
        appRoute ~
        samsungRoute
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

  def appRoute(implicit loggedUser: UserEntity): Route =
    path("app") {
      pathEndOrSingleSlash {
        post {
          fileUpload("json") { case (_, byteSource) =>
            onComplete(importApp(byteSource)) {
              case Success(count) => complete(OK -> count.toString)
              case Failure(e) => handleFailure(e)
            }
          }
        }
      }
    }

  def samsungRoute(implicit loggedUser: UserEntity): Route = {
    def tempDestination(fileInfo: FileInfo): File =
      File.createTempFile(fileInfo.fileName, ".zip")

    path("samsung") {
      pathEndOrSingleSlash {
        post {
          storeUploadedFile("zip", tempDestination) { case (metadata, file) =>
            onComplete(importSamsung(file)) {
              case Success(count) => complete(OK -> count.toString)
              case Failure(e) => handleFailure(e)
            }
          }
        }
      }
    }
  }
}
