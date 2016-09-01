package de.thepiwo.lifelogging

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.t3hnar.bcrypt._
import de.thepiwo.lifelogging.restapi.http.HttpService
import de.thepiwo.lifelogging.restapi.models.UserEntity
import de.thepiwo.lifelogging.restapi.services.{AuthService, LoggingService, UsersService}
import de.thepiwo.lifelogging.restapi.utils.{DatabaseService, JsonProtocol}
import de.thepiwo.lifelogging.utils.TestPostgresDatabase._
import de.thepiwo.lifelogging.utils.TestUserEntity
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

trait BaseServiceTest extends WordSpec with Matchers with ScalatestRouteTest with JsonProtocol {

  private val databaseService = new DatabaseService(jdbcUrl, dbUser, dbPassword)

  val usersService = new UsersService(databaseService)
  val loggingService = new LoggingService(databaseService)
  val authService = new AuthService(databaseService)(usersService)
  val httpService = new HttpService(usersService, authService, loggingService)

  def provisionUsersList(size: Int): Seq[TestUserEntity] = {
    val savedUsers = (1 to size).map { _ =>
      val password = Random.nextString(10)
      (UserEntity(Some(Random.nextLong()), Random.nextString(10), password.bcrypt), password)
    }.map { tuple => usersService.createUser(tuple._1).map { result => TestUserEntity(result, tuple._2) } }

    Await.result(Future.sequence(savedUsers), 10.seconds)
  }

  def provisionTokensForUsers(usersList: Seq[UserEntity]) = {
    val savedTokens = usersList.map(authService.createToken)
    Await.result(Future.sequence(savedTokens), 10.seconds)
  }

}
