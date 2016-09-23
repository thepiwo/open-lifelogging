package de.thepiwo.lifelogging.restapi.services

import de.thepiwo.lifelogging.restapi.models._
import de.thepiwo.lifelogging.restapi.models.db.{CoordEntityTable, WifiEntityTable, LogEntityTable}
import de.thepiwo.lifelogging.restapi.utils.{DatabaseService, Helper}

import scala.concurrent.{ExecutionContext, Future}

class LoggingService(val databaseService: DatabaseService)
                    (implicit executionContext: ExecutionContext)
  extends LogEntityTable with CoordEntityTable with WifiEntityTable {

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
      logEntitiesInserted <- getLogEntitesInsertQuery(logEntityInserted, logEntityInsert)
    } yield LogEntityReturn(
      logEntityInserted.id,
      logEntityInserted.userId,
      logEntityInserted.key,
      LogEntityInsert(logEntitiesInserted),
      logEntityInserted.createdAt
    )
  }


  def getLogKeys(loggedUser: UserEntity): Future[Seq[String]] =
    db.run(logs.filter(_.userId === loggedUser.id).map(_.key).result)

  private def getLogEntitesInsertQuery(logEntityInserted: LogEntity, logEntityInsert: LogEntityInsert): Future[LogEntities] = {
    logEntityInsert.logEntities match {
      case CoordEntity(id, logEntityId, latitude, longitude, altitude, accuracy) =>
        db.run(logCoord returning logCoord += CoordEntity(id, logEntityInserted.id, latitude, longitude, altitude, accuracy))
      case WifiEntity(id, logEntityId, ssid, speed, status) =>
        db.run(logWifi returning logWifi += WifiEntity(id, logEntityInserted.id, ssid, speed, status))
    }
  }

  private def getLogsResult(filterLogEntites: Future[Seq[LogEntity]]): Future[Seq[LogEntityReturn]] =
    for {
      logEntities <- filterLogEntites
      logCoord <- getLogCoord(logEntities.flatMap(_.id))
      logWifi <- getLogWifi(logEntities.flatMap(_.id))
    } yield logEntities.map { logEntity =>
      LogEntityReturn(
        logEntity.id,
        logEntity.userId,
        logEntity.key,
        getLogInsertEntityFromEntites(logEntity, logCoord, logWifi),
        logEntity.createdAt
      )
    }

  private def getLogInsertEntityFromEntites(logEntity: LogEntity, logCoord: Seq[CoordEntity], logWifi: Seq[WifiEntity]): LogEntityInsert = {
    val logCoordEntity = logCoord.find(_.logEntityId == logEntity.id)
    val logWifiEntity = logWifi.find(_.logEntityId == logEntity.id)

    (logCoordEntity, logWifiEntity) match {
      case (Some(coord), None) => LogEntityInsert(coord)
      case (None, Some(wifi)) => LogEntityInsert(wifi)
      case _ => throw new Exception("no or multiple entities found")
    }
  }

  private def getLogCoord(logEntityIds: Seq[Long]): Future[Seq[CoordEntity]] =
    db.run(logCoord.filter(_.logEntityId inSet logEntityIds).result)

  private def getLogWifi(logEntityIds: Seq[Long]): Future[Seq[WifiEntity]] =
    db.run(logWifi.filter(_.logEntityId inSet logEntityIds).result)

}