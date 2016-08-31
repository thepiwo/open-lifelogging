package de.thepiwo.lifelogging.restapi.services

import de.thepiwo.lifelogging.restapi.models.db.TokenEntityTable
import de.thepiwo.lifelogging.restapi.models.{TokenEntity, UserEntity}
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
        password.isBcrypted(user.password) match {
          case true => db.run(tokens.filter(_.userId === user.id).result.headOption).flatMap {
            case Some(token) => Future.successful(token)
            case None => createToken(user)
          }
          case false => throw UnauthorizedException("wrong password")
        }
      case None => throw UnauthorizedException("user not found")
    }
  }

  def signUp(newUser: UserEntity): Future[TokenEntity] = {
    val encryptedPasswordUser = UserEntity(None, newUser.username, newUser.password.bcrypt)
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
