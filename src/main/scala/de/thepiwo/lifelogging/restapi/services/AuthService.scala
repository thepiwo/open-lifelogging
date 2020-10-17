package de.thepiwo.lifelogging.restapi.services

import de.thepiwo.lifelogging.restapi.models.db.TokenEntityTable
import de.thepiwo.lifelogging.restapi.models.{SignUpUser, TokenEntity, UserEntity}
import de.thepiwo.lifelogging.restapi.utils.{DatabaseService, UnauthorizedException}
import com.github.t3hnar.bcrypt._

import scala.concurrent.{ExecutionContext, Future}

class AuthService(val databaseService: DatabaseService)
                 (usersService: UsersService)
                 (implicit executionContext: ExecutionContext) extends TokenEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def signIn(login: String, password: String): Future[TokenEntity] = {
    db.run(users.filter(u => u.username === login).result.headOption).flatMap {
      case Some(user) =>
        if (password.isBcryptedBounded(user.password)) {
          db.run(tokens.filter(_.userId === user.id).result.headOption).flatMap {
            case Some(token) => Future.successful(token)
            case None => createToken(user)
          }
        } else {
          throw UnauthorizedException("wrong password")
        }
      case None => throw UnauthorizedException("user not found")
    }
  }

  def signUp(signUpUser: SignUpUser): Future[TokenEntity] = {
    val encryptedPasswordUser = UserEntity(username = signUpUser.username, password = signUpUser.password.boundedBcrypt)
    usersService.createUser(encryptedPasswordUser).flatMap(user => createToken(user))
  }

  def authenticate(token: String): Future[Option[UserEntity]] =
    db.run((for {
      token <- tokens.filter(_.token === token)
      user <- users.filter(_.id === token.userId)
    } yield user).result.headOption)

  def createToken(user: UserEntity): Future[TokenEntity] =
    db.run(tokens returning tokens += TokenEntity(userId = user.id))

}
