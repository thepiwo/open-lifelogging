package de.thepiwo.lifelogging

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.server.Route
import de.thepiwo.lifelogging.restapi.models.{PublicUserEntity, UserEntityUpdate}
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalatest.concurrent.ScalaFutures


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
      Get(s"/users/${testUser.id.get}") ~> route ~> check {
        responseAs[PublicUserEntity] should be(testUser.public)
      }
    }

    "update user by id and retrieve it" in new Context {
      val testUser = testUsers(3).user
      val newUsername = Random.nextString(10)
      val requestEntity = HttpEntity(MediaTypes.`application/json`,
        UserEntityUpdate(Some(newUsername), None).asJson.noSpaces)

      Post(s"/users/${testUser.id.get}", requestEntity) ~> route ~> check {
        responseAs[PublicUserEntity] should be(testUser.copy(username = newUsername).public)
        whenReady(getUserById(testUser.id.get)) { result =>
          result.get.username should be(newUsername)
        }
      }
    }

    "delete user" in new Context {
      val testUser = testUsers(2).user
      Delete(s"/users/${testUser.id.get}") ~> route ~> check {
        response.status should be(NoContent)
        whenReady(getUserById(testUser.id.get)) { result =>
          result should be(None: Option[PublicUserEntity])
        }
      }
    }

    "retrieve currently logged user" in new Context {
      val testUser = testUsers(1).user
      val header = "Token" -> testTokens.find(_.userId.contains(testUser.id.get)).get.token

      Get("/users/me") ~> addHeader(header._1, header._2) ~> route ~> check {
        responseAs[PublicUserEntity] should be(testUsers.map(_.user).find(_.id.contains(testUser.id.get)).get.public)
      }
    }

    "update currently logged user" in new Context {
      val testUser = testUsers.head.user
      val newUsername = Random.nextString(10)
      val requestEntity = HttpEntity(MediaTypes.`application/json`,
        UserEntityUpdate(Some(newUsername), None).asJson.noSpaces)
      val header = "Token" -> testTokens.find(_.userId.contains(testUser.id.get)).get.token

      Post("/users/me", requestEntity) ~> addHeader(header._1, header._2) ~> route ~> check {
        responseAs[PublicUserEntity] should be(testUsers.map(_.user)
          .find(_.id.contains(testUser.id.get)).get.copy(username = newUsername).public)
        whenReady(getUserById(testUser.id.get)) { result =>
          result.get.username should be(newUsername)
        }
      }
    }

  }

}
