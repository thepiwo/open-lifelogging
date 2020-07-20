package de.thepiwo.lifelogging.restapi.services

import java.lang.Math.{ceil, max}
import java.sql.Timestamp
import java.time.LocalDateTime

import de.thepiwo.lifelogging.restapi.http.routes.DateOptions
import de.thepiwo.lifelogging.restapi.models._
import de.thepiwo.lifelogging.restapi.models.db.LogEntityTable
import de.thepiwo.lifelogging.restapi.utils.Helper._
import de.thepiwo.lifelogging.restapi.utils.{DatabaseService, LogEntityReturn}

import scala.concurrent.{ExecutionContext, Future}

class LoggingService(val databaseService: DatabaseService)
                    (implicit executionContext: ExecutionContext)
  extends LogEntityTable {

  import databaseService._
  import databaseService.driver.api._

  private val UNIFORM_LIMIT: Long = 20000

  private def getLogsQuery(loggedUser: UserEntity, dateOptions: Option[DateOptions]) =
    dateOptions match {
      case None =>
        logs
          .filter(_.hidden === false)
          .filter(_.userId === loggedUser.id)
          .sortBy(_.createdAtClient desc)

      case Some(date) =>
        logs
          .filter(_.hidden === false)
          .filter(_.userId === loggedUser.id)
          .filter(_.createdAtClient
            .between(
              timestampStartDay(date.fromDate),
              timestampEndDay(date.toDate.getOrElse(date.fromDate))
            ))
          .sortBy(_.createdAtClient desc)
    }

  def getLogs(loggedUser: UserEntity, dateOptions: Option[DateOptions], unlimited: Boolean): Future[LogEntityReturn] = {
    val filterLogsQuery: Query[Logs, LogEntity, Seq] = getLogsQuery(loggedUser, dateOptions)
    for {
      count <- getCountLogs(filterLogsQuery)
      logs <- getLimitedLogs(filterLogsQuery, unlimited)
    } yield LogEntityReturn(count, logs)
  }

  def getLogs(loggedUser: UserEntity, logKey: String, dateOptions: Option[DateOptions], unlimited: Boolean): Future[LogEntityReturn] = {
    val filterLogsQuery: Query[Logs, LogEntity, Seq] = getLogsQuery(loggedUser, dateOptions).filter(_.key === logKey)
    for {
      count <- getCountLogs(filterLogsQuery)
      logs <- getLimitedLogs(filterLogsQuery, unlimited)
    } yield LogEntityReturn(count, logs)
  }

  def deleteLogById(loggedUser: UserEntity, logId: Long): Future[Int] =
    db.run(logs.filter(_.userId === loggedUser.id)
      .filter(_.id === logId)
      .map(_.hidden).update(true))

  def getLastLogOlderTwoHours(loggedUser: UserEntity): Future[Int] =
    db.run(logs.filter(_.userId === loggedUser.id)
      .filter(_.createdAtClient > Timestamp.valueOf(LocalDateTime.now().minusHours(2)))
      .distinct.length.result)

  def createLogItem(loggedUser: UserEntity, logEntityInsert: LogEntityInsert): Future[LogEntity] = {
    val logEntity = LogEntity(
      userId = loggedUser.id,
      key = logEntityInsert.key,
      data = logEntityInsert.data,
      createdAtClient = timestamp(logEntityInsert.createdAtClient),
      createdAt = now()
    )

    db.run(logs returning logs += logEntity)
  }

  def createLogItems(loggedUser: UserEntity, logEntitiesInsert: Seq[LogEntityInsert]): Future[Option[Int]] = {
    val logEntities = logEntitiesInsert.map(logEntityInsert => LogEntity(
      userId = loggedUser.id,
      key = logEntityInsert.key,
      data = logEntityInsert.data,
      createdAtClient = timestamp(logEntityInsert.createdAtClient),
      createdAt = now()
    ))

    val logEntitiesMap = logEntities.groupBy(_.createdAtClient)
    val timestamps = logEntities.map(_.createdAtClient)

    for {
      knownTimestamps <- db.run(logs.map(_.createdAtClient).result)

      // optimization for comparing big lists
      groupedTimestamps: Map[Timestamp, Seq[Timestamp]] = timestamps.concat(knownTimestamps).groupBy(identity)
      nonDuplicates = groupedTimestamps
        .collect { case (x, Seq(_)) => logEntitiesMap.get(x).map(_.head) }
        .filter(_.nonEmpty).map(_.get).toSeq
      inserted <- db.run((logs ++= nonDuplicates).transactionally)
    } yield inserted
  }

  private def getCountLogs(filterLogsQuery: Query[Logs, LogEntity, Seq]): Future[Int] = db.run(filterLogsQuery.size.result)

  private def getLimitedLogs(filterLogsQuery: Query[Logs, LogEntity, Seq], unlimited: Boolean): Future[Seq[LogEntity]] =
    if (unlimited) {
      db.run(filterLogsQuery.sortBy(_.createdAtClient desc).result)
    } else {
      getValuesForLimit(filterLogsQuery).flatMap { case (maxId, minId, modSelector) =>
        val limitedLogsQuery = getLimitedLogs(filterLogsQuery, maxId, minId, modSelector, UNIFORM_LIMIT - 2)
        db.run(limitedLogsQuery.sortBy(_.createdAtClient desc).result)
      }
    }

  private def getLimitedLogs(filterLogsQuery: Query[Logs, LogEntity, Seq], maxId: Long, minId: Long, modSelector: Long, limit: Long): Query[Logs, LogEntity, Seq] = {
    val filterLogsQueryWithIndex = filterLogsQuery.map(_.id).zipWithIndex

    val idSet = filterLogsQueryWithIndex
      .filter { case (id, row) =>
        val modulo = max(ceil(modSelector.toDouble / limit.toDouble).toLong, 1)
        id === maxId || id === minId || (row % modulo) === 0L
      }.map(_._1)

    logs.filter(_.id in idSet)
  }

  private def getValuesForLimit(filterLogsQuery: Query[Logs, LogEntity, Seq]): Future[(Long, Long, Long)] =
    db.run(filterLogsQuery.map(_.id).zipWithIndex.result).map { idWithIndex: Seq[(Long, Long)] =>
      val modSelector = idWithIndex.map(_._2) |> emptyZeroOrMax
      val maxId = idWithIndex.map(_._1) |> emptyZeroOrMax
      val minId = idWithIndex.map(_._1) |> emptyZeroOrMin

      (maxId, minId, modSelector)
    }

  def getLatestLog(loggedUser: UserEntity, logKey: String): Future[Option[LogEntity]] = {
    val limitLogsQuery: Query[Logs, LogEntity, Seq] =
      logs
        .filter(_.hidden === false)
        .filter(_.userId === loggedUser.id)
        .filter(_.key === logKey)
        .sortBy(_.createdAtClient desc)

    db.run(limitLogsQuery.take(1).result.map(_.headOption))
  }

  def getLatestLogs(loggedUser: UserEntity, limitOption: Option[Long]): Future[LogEntityReturn] = {
    val limit = limitOption match {
      case Some(value) => value
      case None => UNIFORM_LIMIT
    }

    val limitLogsQuery: Query[Logs, LogEntity, Seq] =
      logs
        .filter(_.hidden === false)
        .filter(_.userId === loggedUser.id)
        .sortBy(_.createdAtClient desc)

    for {
      count <- getCountLogs(limitLogsQuery)
      logs <- db.run(limitLogsQuery.take(limit).result)
    } yield LogEntityReturn(count, logs)
  }

  def getLogKeys(loggedUser: UserEntity): Future[Seq[String]] =
    db.run(logs.filter(_.hidden === false).filter(_.userId === loggedUser.id).map(_.key).result)

}