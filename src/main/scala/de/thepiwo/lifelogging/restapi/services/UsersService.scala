package de.thepiwo.lifelogging.restapi.services

import de.thepiwo.lifelogging.restapi.models.db.UserEntityTable
import de.thepiwo.lifelogging.restapi.models.{UserEntity, UserEntityUpdate}
import de.thepiwo.lifelogging.restapi.utils.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

class UsersService(val databaseService: DatabaseService)
                  (implicit executionContext: ExecutionContext) extends UserEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getUsers(): Future[Seq[UserEntity]] =
    db.run(users.result)

  def getUserById(id: Long): Future[Option[UserEntity]] =
    db.run(users.filter(_.id === id).result.headOption)

  def getUserByLogin(login: String): Future[Option[UserEntity]] =
    db.run(users.filter(_.username === login).result.headOption)

  def createUser(user: UserEntity): Future[UserEntity] =
    db.run(users returning users += user)

  def updateUser(id: Long, userUpdate: UserEntityUpdate): Future[Option[UserEntity]] =
    getUserById(id).flatMap {
      case Some(user) =>
        val updatedUser = userUpdate.merge(user)
        db.run(users.filter(_.id === id).update(updatedUser)).map(_ => Some(updatedUser))
      case None => Future.successful(None)
    }

  def deleteUser(id: Long): Future[Int] =
    db.run(users.filter(_.id === id).delete)

}