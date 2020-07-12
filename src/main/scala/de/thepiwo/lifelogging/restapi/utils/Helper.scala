package de.thepiwo.lifelogging.restapi.utils

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

import scala.util.Try

object Helper {

  def now(): Timestamp = Timestamp.valueOf(LocalDateTime.now())

  def timestamp(createdAtClient: Long): Timestamp =
    Timestamp.valueOf(Instant.ofEpochMilli(createdAtClient).atOffset(ZoneOffset.UTC).toLocalDateTime)

  def timestampStartDay(localDate: LocalDate): Timestamp =
    Timestamp.valueOf(localDate.atStartOfDay())

  def timestampEndDay(localDate: LocalDate): Timestamp =
    Timestamp.valueOf(localDate.plusDays(1).atStartOfDay())

  def localDate(dateString: String): Option[LocalDate] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    Try(LocalDate.parse(dateString, formatter)).toOption
  }

  implicit final class Pipe[T](val x: T) extends AnyVal {
    def |>[B](f: T => B) = f(x)
  }

  def emptyZeroOrMax(list: Seq[Long]): Long =
    list match {
      case Nil => 0
      case nonEmpty => nonEmpty.max
    }

  def emptyZeroOrMin(list: Seq[Long]): Long =
    list match {
      case Nil => 0
      case nonEmpty => nonEmpty.min
    }

}
