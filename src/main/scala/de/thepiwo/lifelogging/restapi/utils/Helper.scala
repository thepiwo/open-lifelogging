package de.thepiwo.lifelogging.restapi.utils

import java.security.MessageDigest
import java.sql.Timestamp
import java.time.{Instant, LocalDateTime, ZoneOffset}

import spray.json.JsValue

object Helper {

  def now(): Timestamp = Timestamp.valueOf(LocalDateTime.now())

  def timestamp(createdAtClient: Long) =
    Timestamp.valueOf(Instant.ofEpochMilli(createdAtClient).atOffset(ZoneOffset.UTC).toLocalDateTime)

  def getJsonHash(data: JsValue): String = {
    val dataBytes: Array[Byte] = data.toString.getBytes("utf-8")
    val hash = MessageDigest.getInstance("SHA-256").digest(dataBytes)
    hash.map("%02x".format(_)).mkString
  }

}
