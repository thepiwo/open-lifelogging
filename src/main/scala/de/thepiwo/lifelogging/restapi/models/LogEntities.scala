package de.thepiwo.lifelogging.restapi.models

sealed trait LogEntities

case class CoordEntity(id: Option[Long] = None,
                       logEntityId: Option[Long],
                       latitude: Double,
                       longitude: Double,
                       altitude: Double,
                       accuracy: Float) extends LogEntities

case class WifiEntity(id: Option[Long] = None,
                      logEntityId: Option[Long],
                      ssid: String,
                      speed: Int,
                      status: String) extends LogEntities
