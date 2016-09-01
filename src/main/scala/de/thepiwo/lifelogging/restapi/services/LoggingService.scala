package de.thepiwo.lifelogging.restapi.services

import de.thepiwo.lifelogging.restapi.models._
import de.thepiwo.lifelogging.restapi.models.db.{LogCoordEntityTable, LogEntityTable}
import de.thepiwo.lifelogging.restapi.utils.{DatabaseService, Helper}

import scala.concurrent.{ExecutionContext, Future}

class LoggingService(val databaseService: DatabaseService)
                    (implicit executionContext: ExecutionContext) extends LogEntityTable with LogCoordEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getLogs(loggedUser: UserEntity): Future[Seq[LogEntityReturn]] =
    getLogsResult(db.run(logs.filter(_.userId === loggedUser.id).result))

  def getLogs(loggedUser: UserEntity, logKey: String): Future[Seq[LogEntityReturn]] =
    getLogsResult(db.run(logs
      .filter(_.userId === loggedUser.id)
      .filter(_.key === logKey).result))

  def createLogItem(loggedUser: UserEntity, logKey: String, logEntityInsert: LogEntityInsert): Future[LogEntityReturn] = {
    val logEntity = LogEntity(None, loggedUser.id, logKey, Helper.now())

    for {
      logEntityInserted <- db.run(logs returning logs += logEntity)
      logCoordsEntity <- createLogCoordsEntity(logEntityInsert, logEntityInserted)
      logCoordsEntityInserted <- db.run(logCoords returning logCoords += logCoordsEntity)
    } yield LogEntityReturn(
      logEntityInserted.id,
      logEntityInserted.userId,
      logEntityInserted.key,
      Some(logCoordsEntityInserted),
      logEntityInserted.createdAt
    )
  }

  def getLogKeys(loggedUser: UserEntity): Future[Seq[String]] =
    db.run(logs.filter(_.userId === loggedUser.id).map(_.key).result)

  private def getLogsResult(filterLogEntites: Future[Seq[LogEntity]]): Future[Seq[LogEntityReturn]] =
    for {
      logEntities <- filterLogEntites
      logCoords <- getLogCoords(logEntities.flatMap(_.id))
    } yield logEntities.map { logEntity =>
      LogEntityReturn(
        logEntity.id,
        logEntity.userId,
        logEntity.key,
        logCoords.find(_.logEntityId == logEntity.id),
        logEntity.createdAt
      )
    }

  private def getLogCoords(logEntityIds: Seq[Long]): Future[Seq[LogCoordEntity]] =
    db.run(logCoords.filter(_.logEntityId inSet logEntityIds).result)

  private def createLogCoordsEntity(logEntityInsert: LogEntityInsert,
                                    logEntityInserted: LogEntity): Future[LogCoordEntity] =
    Future.successful(
      LogCoordEntity(
        None,
        logEntityInserted.id,
        logEntityInsert.logCoordEntity.get.latitude,
        logEntityInsert.logCoordEntity.get.longitude
      )
    )

}