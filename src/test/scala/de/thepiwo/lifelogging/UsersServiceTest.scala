package de.thepiwo.lifelogging

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.server.Route
import de.thepiwo.lifelogging.restapi.models.{PublicUserEntity, UserEntityUpdate}
import org.scalatest.concurrent.ScalaFutures
import spray.json._

import scala.util.Random

class UsersServiceTest extends BaseServiceTest with ScalaFutures {

  import usersService._

  trait Context {
    val testUsers = provisionUsersList(5)
    val testTokens = provisionTokensForUsers(testUsers.map(_.user))
    val route: Route = httpService.usersRouter.route
  }

  "Users service" should {

    "retrieve users list" in new Context {
      Get("/users") ~> route ~> check {
        responseAs[Seq[PublicUserEntity]].isEmpty should be(false)
      }
    }

    "retrieve user by id" in new Context {
      val testUser = testUsers(4).user
      Get(s"/users/${testUser.id}") ~> route ~> check {
        responseAs[PublicUserEntity] should be(testUser.public)
      }
    }

    "delete currently logged user" in new Context {
      val testUser = testUsers(2).user
      val header = "Token" -> testTokens.find(_.userId == testUser.id).get.token

      Delete(s"/users/me") ~> addHeader(header._1, header._2) ~> route ~> check {
        response.status should be(OK)
        whenReady(getUserById(testUser.id)) { result =>
          result should be(None: Option[PublicUserEntity])
        }
      }
    }

    "retrieve currently logged user" in new Context {
      val testUser = testUsers(1).user
      val header = "Token" -> testTokens.find(_.userId == testUser.id).get.token

      Get("/users/me") ~> addHeader(header._1, header._2) ~> route ~> check {
        responseAs[PublicUserEntity] should be(testUsers.map(_.user).find(_.id == testUser.id).get.public)
      }
    }

    "update currently logged user" in new Context {
      val testUser = testUsers.head.user
      val newUsername = Random.nextString(10)
      val requestEntity = HttpEntity(MediaTypes.`application/json`,
        UserEntityUpdate(Some(newUsername), None).toJson.compactPrint)
      val header = "Token" -> testTokens.find(_.userId == testUser.id).get.token

      Post("/users/me", requestEntity) ~> addHeader(header._1, header._2) ~> route ~> check {
        responseAs[PublicUserEntity] should be(testUsers.map(_.user)
          .find(_.id == testUser.id).get.copy(username = newUsername).public)
        whenReady(getUserById(testUser.id)) { result =>
          result.get.username should be(newUsername)
        }
      }
    }

  }

}
