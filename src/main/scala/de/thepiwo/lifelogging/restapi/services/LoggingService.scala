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

  private val UNIFORM_LIMIT: Long = 2000

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

  def getLogs(loggedUser: UserEntity, dateOptions: Option[DateOptions], unlimited: Boolean): Future[Seq[LogEntity]] = {
    val filterLogsQuery: Query[Logs, LogEntity, Seq] = getLogsQuery(loggedUser, dateOptions)
    getLimitedLogs(filterLogsQuery, unlimited)
  }

  def getLogs(loggedUser: UserEntity, logKey: String, dateOptions: Option[DateOptions], unlimited: Boolean): Future[Seq[LogEntity]] = {
    val filterLogsQuery: Query[Logs, LogEntity, Seq] = getLogsQuery(loggedUser, dateOptions).filter(_.key === logKey)
    getLimitedLogs(filterLogsQuery, unlimited)
  }

  def deleteLogById(loggedUser: UserEntity, logId: Long): Future[Int] =
    db.run(logs.filter(_.userId === loggedUser.id)
      .filter(_.id === logId)
      .delete)

  def getLastLogOlderTwoHours(loggedUser: UserEntity): Future[Int] =
    db.run(logs.filter(_.userId === loggedUser.id)
      .filter(_.createdAtClient > Timestamp.valueOf(LocalDateTime.now().minusHours(2)))
      .distinct.length.result)

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

  private def getLimitedLogs(filterLogsQuery: Query[Logs, LogEntity, Seq], unlimited: Boolean): Future[Seq[LogEntity]] =
    if (unlimited) {
      db.run(filterLogsQuery.sortBy(_.createdAt desc).result)
    } else {
      getValuesForLimit(filterLogsQuery).flatMap { case (maxId, minId, modSelector) =>
        val limitedLogsQuery = getLimitedLogs(filterLogsQuery, maxId, minId, modSelector, UNIFORM_LIMIT - 2)
        db.run(limitedLogsQuery.sortBy(_.createdAt desc).result)
      }
    }

  private def getLimitedLogs(filterLogsQuery: Query[Logs, LogEntity, Seq], maxId: Long, minId: Long, modSelector: Long, limit: Long): Query[Logs, LogEntity, Seq] = {
    val filterLogsQueryWithIndex = filterLogsQuery.map(_.id).zipWithIndex

    val idSet = filterLogsQueryWithIndex
      .filter { case (id, row) =>
        val modulo = Math.max(Math.ceil(modSelector / limit).toLong, 1)
        id === maxId || id === minId || (row % modulo) === 0L
      }.map(_._1)

    logs.filter(_.id in idSet)
  }

  private def getValuesForLimit(filterLogsQuery: Query[Logs, LogEntity, Seq]): Future[(Long, Long, Long)] =
    db.run(filterLogsQuery.map(_.id).zipWithIndex.result).map { idWithIndex: Seq[(Option[Long], Long)] =>
      val modSelector = idWithIndex.map(_._2).max
      val maxId = idWithIndex.map(_._1.getOrElse(0L)).max
      val minId = idWithIndex.map(_._1.getOrElse(0L)).min

      (maxId, minId, modSelector)
    }


  def getLogKeys(loggedUser: UserEntity): Future[Seq[String]] =
    db.run(logs.filter(_.userId === loggedUser.id).map(_.key).result)
}