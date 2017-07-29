package de.thepiwo.lifelogging.restapi.services

import java.sql.Timestamp
import java.time.LocalDateTime

import de.thepiwo.lifelogging.restapi.http.routes.DateOptions
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

  private def getLogsQuery(loggedUser: UserEntity, dateOptions: Option[DateOptions]) =
    dateOptions match {
      case None =>
        logs
          .filter(_.userId === loggedUser.id)
          .sortBy(_.createdAt desc)

      case Some(date) =>
        logs
          .filter(_.userId === loggedUser.id)
          .filter(_.createdAtClient
            .between(
              timestampStartDay(date.fromDate),
              timestampEndDay(date.toDate.getOrElse(date.fromDate))
            ))
          .sortBy(_.createdAt desc)
    }

  def getLogs(loggedUser: UserEntity, dateOptions: Option[DateOptions]): Future[Seq[LogEntity]] =
    db.run(getLogsQuery(loggedUser, dateOptions).result)

  def deleteLogById(loggedUser: UserEntity, logId: Long): Future[Int] =
    db.run(logs.filter(_.userId === loggedUser.id)
      .filter(_.id === logId)
      .delete)

  def getLastLogOlderTwoHours(loggedUser: UserEntity): Future[Int] =
    db.run(logs.filter(_.userId === loggedUser.id)
      .filter(_.createdAtClient > Timestamp.valueOf(LocalDateTime.now().minusHours(2)))
      .distinct.length.result)

  def getLogs(loggedUser: UserEntity, logKey: String, dateOptions: Option[DateOptions]): Future[Seq[LogEntity]] =
    db.run(getLogsQuery(loggedUser, dateOptions).filter(_.key === logKey).result)

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