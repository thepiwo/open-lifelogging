package de.thepiwo.lifelogging

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.t3hnar.bcrypt._
import de.thepiwo.lifelogging.restapi.http.HttpService
import de.thepiwo.lifelogging.restapi.models.{TokenEntity, UserEntity}
import de.thepiwo.lifelogging.restapi.services.{AuthService, ImportService, LoggingService, UsersService}
import de.thepiwo.lifelogging.restapi.utils.{DatabaseService, JsonProtocol}
import de.thepiwo.lifelogging.utils.TestPostgresDatabase._
import de.thepiwo.lifelogging.utils.TestUserEntity
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

trait BaseServiceTest extends AnyWordSpec with Matchers with ScalatestRouteTest with JsonProtocol {

  private val databaseService = new DatabaseService(jdbcUrl, dbUser, dbPassword)

  val usersService = new UsersService(databaseService)
  val loggingService = new LoggingService(databaseService)
  val authService = new AuthService(databaseService)(usersService)
  val importService = new ImportService(databaseService, loggingService)
  val httpService = new HttpService(usersService, authService, loggingService, importService)

  def provisionUsersList(size: Int): Seq[TestUserEntity] = {
    val savedUsers = (1 to size).map { _ =>
      val password = Random.nextString(10)
      (UserEntity(Random.nextLong(), Random.nextString(10), password.boundedBcrypt), password)
    }.map { tuple => usersService.createUser(tuple._1).map { result => TestUserEntity(result, tuple._2) } }

    Await.result(Future.sequence(savedUsers), 10.seconds)
  }

  def provisionTokensForUsers(usersList: Seq[UserEntity]): Seq[TokenEntity] = {
    val savedTokens = usersList.map(authService.createToken)
    Await.result(Future.sequence(savedTokens), 10.seconds)
  }

}
