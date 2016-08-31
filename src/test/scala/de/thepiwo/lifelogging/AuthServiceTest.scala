package de.thepiwo.lifelogging

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.server
import de.thepiwo.lifelogging.restapi.models.{TokenEntity, UserEntity}
import de.thepiwo.lifelogging.restapi.utils.LoginPassword
import de.thepiwo.lifelogging.utils.TestUserEntity
import io.circe.generic.auto._
import io.circe.syntax._


class AuthServiceTest extends BaseServiceTest {

  trait Context {
    val testUsers = provisionUsersList(2)
    val route = httpService.authRouter.route
  }

  "Auth service" should {

    "register users and retrieve token" in new Context {
      val testUser = testUsers.head
      signUpUser(testUser.user, route) {
        response.status should be(StatusCodes.Created)
      }
    }

    "authorize users by login and password and retrieve token" in new Context {
      val testUser = testUsers(1)
      signInUser(testUser, route) {
        responseAs[TokenEntity] should be
      }
    }

  }

  private def signUpUser(user: UserEntity, route: server.Route)(action: => Unit) = {
    val requestEntity = HttpEntity(MediaTypes.`application/json`, user.asJson.noSpaces)
    Post("/auth/signUp", requestEntity) ~> route ~> check(action)
  }

  private def signInUser(testUser: TestUserEntity, route: server.Route)(action: => Unit) = {
    val requestEntity = HttpEntity(
      MediaTypes.`application/json`,
      LoginPassword(testUser.user.username, testUser.password).asJson.noSpaces
    )
    Post("/auth/signIn", requestEntity) ~> route ~> check(action)
  }

}
