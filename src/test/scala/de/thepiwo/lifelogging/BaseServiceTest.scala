package de.thepiwo.lifelogging

import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.CirceSupport
import de.thepiwo.lifelogging.restapi.http.HttpService
import de.thepiwo.lifelogging.restapi.models.UserEntity
import de.thepiwo.lifelogging.restapi.services.{AuthService, UsersService}
import de.thepiwo.lifelogging.restapi.utils.DatabaseService
import de.thepiwo.lifelogging.utils.TestPostgresDatabase._
import org.apache.commons.lang3.RandomStringUtils
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random
import com.github.t3hnar.bcrypt._
import de.thepiwo.lifelogging.utils.TestUserEntity

trait BaseServiceTest extends WordSpec with Matchers with ScalatestRouteTest with CirceSupport {

  private val databaseService = new DatabaseService(jdbcUrl, dbUser, dbPassword)

  val usersService = new UsersService(databaseService)
  val authService = new AuthService(databaseService)(usersService)
  val httpService = new HttpService(usersService, authService)

  def provisionUsersList(size: Int): Seq[TestUserEntity] = {
    val savedUsers = (1 to size).map { _ =>
      val password = RandomStringUtils.randomAscii(10)
      (UserEntity(Some(Random.nextLong()), RandomStringUtils.randomAlphanumeric(10), password.bcrypt), password)
    }.map { tuple => usersService.createUser(tuple._1).map { result => TestUserEntity(result, tuple._2) } }

    Await.result(Future.sequence(savedUsers), 10.seconds)
  }

  def provisionTokensForUsers(usersList: Seq[UserEntity]) = {
    val savedTokens = usersList.map(authService.createToken)
    Await.result(Future.sequence(savedTokens), 10.seconds)
  }

}
