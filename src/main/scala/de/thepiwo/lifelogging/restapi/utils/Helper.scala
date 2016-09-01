package de.thepiwo.lifelogging.restapi.utils

import java.sql.Timestamp
import java.time.LocalDateTime

object Helper {
  def now(): Timestamp = Timestamp.valueOf(LocalDateTime.now())
}
