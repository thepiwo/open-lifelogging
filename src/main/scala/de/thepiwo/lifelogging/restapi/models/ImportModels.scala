package de.thepiwo.lifelogging.restapi.models

case class GoogleLocation(timestampMs: String, latitudeE7: Float, longitudeE7: Float, accuracy: Float)

case class GoogleLocations(locations: Seq[GoogleLocation])

case class CoordEntity(accuracy: Option[Float], latitude: Float, longitude: Float, source: Option[String], altitude: Option[Float])

case class SamsungLocation(start_time: Long, latitude: Float, longitude: Float, altitude: Option[Float])

case class AppLocation(createdAtClient: Long, data: CoordEntity)