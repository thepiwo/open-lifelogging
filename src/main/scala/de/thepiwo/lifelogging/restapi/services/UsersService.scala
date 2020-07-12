package de.thepiwo.lifelogging.restapi.services

import de.thepiwo.lifelogging.restapi.models.db.{UserSettingsEntityTable, UserEntityTable}
import de.thepiwo.lifelogging.restapi.models.{PublicUserEntity, UserEntity, UserSettingsEntity, UserEntityUpdate}
import de.thepiwo.lifelogging.restapi.utils.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

class UsersService(val databaseService: DatabaseService)
                  (implicit executionContext: ExecutionContext) extends UserEntityTable with UserSettingsEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getUsers(): Future[Seq[PublicUserEntity]] =
    db.run(users.result).map { users => users.map(_.public) }

  def getUserById(id: Long): Future[Option[PublicUserEntity]] =
    db.run(users.filter(_.id === id).result.headOption).map { user => user.map(_.public) }

  def getUserByLogin(login: String): Future[Option[PublicUserEntity]] =
    db.run(users.filter(_.username === login).result.headOption).map { user => user.map(_.public) }

  def createUser(user: UserEntity): Future[UserEntity] =
    db.run(users returning users += user)

  def updateUser(loggedUser: UserEntity, userUpdate: UserEntityUpdate): Future[Option[PublicUserEntity]] =
    getInternalUserById(loggedUser.id).flatMap {
      case Some(user) =>
        val updatedUser: UserEntity = userUpdate.merge(user)
        db.run(users.filter(_.id === loggedUser.id).update(updatedUser)).map(_ => Some(updatedUser.public))
      case None => Future.successful(None)
    }

  def deleteUser(loggedUser: UserEntity): Future[Int] =
    db.run(users.filter(_.id === loggedUser.id).delete)

  private def getInternalUserById(id: Long): Future[Option[UserEntity]] =
    db.run(users.filter(_.id === id).result.headOption)

  def getLastFmUsers: Future[Seq[(String, UserEntity)]] =
    db.run((for {
      us <- userSettings.filter(_.lastFmUsername.isDefined)
      u <- users.filter(_.id === us.userId)
    } yield (us.lastFmUsername.get, u)).result)
}