package de.thepiwo.lifelogging.restapi.utils

import java.security.MessageDigest
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

import spray.json.JsValue

import scala.util.Try

object Helper {

  def now(): Timestamp = Timestamp.valueOf(LocalDateTime.now())

  def timestamp(createdAtClient: Long): Timestamp =
    Timestamp.valueOf(Instant.ofEpochMilli(createdAtClient).atOffset(ZoneOffset.UTC).toLocalDateTime)

  def timestampStartDay(localDate: LocalDate): Timestamp =
    Timestamp.valueOf(localDate.atStartOfDay())

  def timestampEndDay(localDate: LocalDate): Timestamp =
    Timestamp.valueOf(localDate.plusDays(1).atStartOfDay())

  def getJsonHash(data: JsValue): String = {
    val dataBytes: Array[Byte] = data.toString.getBytes("utf-8")
    val hash = MessageDigest.getInstance("SHA-256").digest(dataBytes)
    hash.map("%02x".format(_)).mkString
  }

  def localDate(dateString: String): Option[LocalDate] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    Try(LocalDate.parse(dateString, formatter)).toOption
  }

}
