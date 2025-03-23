package de.thepiwo.lifelogging.restapi.models

case class GoogleLocations(semanticSegments: Seq[GoogleSemanticSegment], rawSignals: Seq[GoogleRawSignal])

case class GoogleSemanticSegment(timelinePath: Option[Seq[GoogleTimelinePoint]])

case class GoogleRawSignal(position: Option[GoogleSignalPosition])

case class GoogleTimelinePoint(time: String, point: String)

case class GoogleSignalPosition(timestamp: String, LatLng: String, accuracyMeters: Float)

case class CoordEntity(accuracy: Option[Float], latitude: Float, longitude: Float, source: Option[String], altitude: Option[Float])

case class SamsungLocation(start_time: Long, latitude: Float, longitude: Float, altitude: Option[Float])

case class AppLocation(createdAtClient: Long, data: CoordEntity)