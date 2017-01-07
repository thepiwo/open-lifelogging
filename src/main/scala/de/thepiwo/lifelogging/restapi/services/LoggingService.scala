package de.thepiwo.lifelogging.restapi.services

import de.thepiwo.lifelogging.restapi.models._
import de.thepiwo.lifelogging.restapi.models.db.LogEntityTable
import de.thepiwo.lifelogging.restapi.utils.{DatabaseService, Helper}

import scala.concurrent.{ExecutionContext, Future}

class LoggingService(val databaseService: DatabaseService)
                    (implicit executionContext: ExecutionContext)
  extends LogEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getLogs(loggedUser: UserEntity): Future[Seq[LogEntity]] =
    db.run(logs
      .filter(_.userId === loggedUser.id)
      .sortBy(_.createdAt desc).result)

  def getLogs(loggedUser: UserEntity, logKey: String): Future[Seq[LogEntity]] =
    db.run(logs
      .filter(_.userId === loggedUser.id)
      .filter(_.key === logKey)
      .sortBy(_.createdAt desc).result)

  def createLogItem(loggedUser: UserEntity, logEntityInsert: LogEntityInsert): Future[LogEntity] = {
    val logEntity = LogEntity(None, loggedUser.id, logEntityInsert.key, logEntityInsert.data, Helper.now())
    db.run(logs returning logs += logEntity)
  }


  def getLogKeys(loggedUser: UserEntity): Future[Seq[String]] =
    db.run(logs.filter(_.userId === loggedUser.id).map(_.key).result)
}