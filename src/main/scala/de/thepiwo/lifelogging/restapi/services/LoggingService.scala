package de.thepiwo.lifelogging.restapi.services

import java.time.LocalDate

import de.thepiwo.lifelogging.restapi.models._
import de.thepiwo.lifelogging.restapi.models.db.LogEntityTable
import de.thepiwo.lifelogging.restapi.utils.Helper.{timestampEndDay, timestampStartDay}
import de.thepiwo.lifelogging.restapi.utils.{DatabaseService, Helper}

import scala.concurrent.{ExecutionContext, Future}

class LoggingService(val databaseService: DatabaseService)
                    (implicit executionContext: ExecutionContext)
  extends LogEntityTable {

  import databaseService._
  import databaseService.driver.api._

  private def getLogsQuery(loggedUser: UserEntity, dateOption: Option[LocalDate]) =
    dateOption match {
      case None =>
        logs
          .filter(_.userId === loggedUser.id)
          .sortBy(_.createdAt desc)

      case Some(localDate) =>
        logs
          .filter(_.userId === loggedUser.id)
          .filter(_.createdAtClient.between(timestampStartDay(localDate), timestampEndDay(localDate)))
          .sortBy(_.createdAt desc)
    }

  def getLogs(loggedUser: UserEntity, dateOption: Option[LocalDate]): Future[Seq[LogEntity]] =
    db.run(getLogsQuery(loggedUser, dateOption).result)

  def getLogs(loggedUser: UserEntity, logKey: String, dateOption: Option[LocalDate]): Future[Seq[LogEntity]] =
    db.run(getLogsQuery(loggedUser, dateOption).filter(_.key === logKey).result)

  def createLogItem(loggedUser: UserEntity, logEntityInsert: LogEntityInsert): Future[LogEntity] = {
    val logEntity = LogEntity(None,
      userId = loggedUser.id,
      key = logEntityInsert.key,
      data = logEntityInsert.data,
      hash = Helper.getJsonHash(logEntityInsert.data),
      createdAtClient = Helper.timestamp(logEntityInsert.createdAtClient),
      createdAt = Helper.now()
    )

    db.run(logs returning logs += logEntity)
  }

  def getLogKeys(loggedUser: UserEntity): Future[Seq[String]] =
    db.run(logs.filter(_.userId === loggedUser.id).map(_.key).result)
}