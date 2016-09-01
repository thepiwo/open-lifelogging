package de.thepiwo.lifelogging.restapi.services

import de.thepiwo.lifelogging.restapi.models._
import de.thepiwo.lifelogging.restapi.models.db.{LogCoordEntityTable, LogEntityTable}
import de.thepiwo.lifelogging.restapi.utils.{DatabaseService, Helper}

import scala.concurrent.{ExecutionContext, Future}

class LoggingService(val databaseService: DatabaseService)
                    (implicit executionContext: ExecutionContext) extends LogEntityTable with LogCoordEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getLogs(loggedUser: UserEntity): Future[Seq[LogEntityReturn]] = ???

  def getLogs(loggedUser: UserEntity, logKey: String): Future[Seq[LogEntityReturn]] = ???


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

  def getLogKeys(): Future[Seq[String]] = ???


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