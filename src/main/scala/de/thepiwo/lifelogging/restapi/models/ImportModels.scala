package de.thepiwo.lifelogging.restapi.models

case class GoogleLocation(timestampMs: String, latitudeE7: Float, longitudeE7: Float, accuracy: Float)

case class GoogleLocations(locations: Seq[GoogleLocation])

case class CoordEntity(accuracy: Float, latitude: Float, longitude: Float)