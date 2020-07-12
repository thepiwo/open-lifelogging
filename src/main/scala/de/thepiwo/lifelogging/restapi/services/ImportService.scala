package de.thepiwo.lifelogging.restapi.services

import akka.stream.Materializer
import akka.stream.scaladsl.{Framing, Sink, Source}
import akka.util.ByteString
import de.thepiwo.lifelogging.restapi.models._
import de.thepiwo.lifelogging.restapi.models.db.TokenEntityTable
import de.thepiwo.lifelogging.restapi.utils.DatabaseService
import spray.json.{NullOptions, RootJsonFormat, _}

import scala.concurrent.{ExecutionContext, Future}

object ImportJsonProtocol extends DefaultJsonProtocol with NullOptions {
  implicit val googleLocationFormat: RootJsonFormat[GoogleLocation] = jsonFormat4(GoogleLocation)
  implicit val googleLocationsFormat: RootJsonFormat[GoogleLocations] = jsonFormat1(GoogleLocations)
  implicit val coordEntityFormat: RootJsonFormat[CoordEntity] = jsonFormat3(CoordEntity)
}

class ImportService(val databaseService: DatabaseService, val loggingService: LoggingService)
                   (implicit val executionContext: ExecutionContext, implicit val materializer: Materializer) extends TokenEntityTable {

  import ImportJsonProtocol._

  def importGoogle(byteSource: Source[ByteString, Any])(implicit user: UserEntity): Future[Int] =
    for {
      dataString <- byteSource
        .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
        .map(_.utf8String)
        .runWith(Sink.seq)

      locations = dataString.mkString.parseJson.convertTo[GoogleLocations].locations
      logEntries = locations.filter(_.accuracy < 100).map(location => LogEntityInsert("CoordEntity",
        CoordEntity(
          accuracy = location.accuracy,
          latitude = location.latitudeE7 / 10000000,
          longitude = location.longitudeE7 / 10000000).toJson, location.timestampMs.toLong))

      inserted <- loggingService.createLogItems(user, logEntries)
    } yield inserted.getOrElse(0)
}
