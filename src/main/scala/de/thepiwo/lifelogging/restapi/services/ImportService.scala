package de.thepiwo.lifelogging.restapi.services

import akka.stream.Materializer
import akka.stream.scaladsl.{Framing, Sink, Source => StreamSource}
import akka.util.ByteString
import de.thepiwo.lifelogging.restapi.models.db.TokenEntityTable
import de.thepiwo.lifelogging.restapi.models.{AppLocation, _}
import de.thepiwo.lifelogging.restapi.utils.DatabaseService
import spray.json.{NullOptions, RootJsonFormat, _}

import java.io.File
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}
import scala.io.{Source => IoSource}
import scala.languageFeature.existentials
import scala.reflect.io.ZipArchive

object ImportJsonProtocol extends DefaultJsonProtocol with NullOptions {
  implicit val googleSignalPositionFormat: RootJsonFormat[GoogleSignalPosition] = jsonFormat3(GoogleSignalPosition)
  implicit val googleTimelinePointFormat: RootJsonFormat[GoogleTimelinePoint] = jsonFormat2(GoogleTimelinePoint)
  implicit val googleSemanticSegmentFormat: RootJsonFormat[GoogleSemanticSegment] = jsonFormat1(GoogleSemanticSegment)
  implicit val googleRawSignalFormat: RootJsonFormat[GoogleRawSignal] = jsonFormat1(GoogleRawSignal)
  implicit val googleLocationsFormat: RootJsonFormat[GoogleLocations] = jsonFormat2(GoogleLocations)
  implicit val coordEntityFormat: RootJsonFormat[CoordEntity] = jsonFormat5(CoordEntity)
  implicit val samsungLocationFormat: RootJsonFormat[SamsungLocation] = jsonFormat4(SamsungLocation)
  implicit val appLocationFormat: RootJsonFormat[AppLocation] = jsonFormat2(AppLocation)
}

class ImportService(val databaseService: DatabaseService, val loggingService: LoggingService)
                   (implicit val executionContext: ExecutionContext, implicit val materializer: Materializer) extends TokenEntityTable {

  import ImportJsonProtocol._

  private[ImportService] def parseGoogleLatLng(latLng: String): (Float, Float) = {
    val parsedArray: Array[Float] = latLng.split(", ").map(_.replaceAll("°", "").toFloat)
    (parsedArray(0), parsedArray(1))
  }

  def importGoogle(byteSource: StreamSource[ByteString, Any])(implicit user: UserEntity): Future[Int] =
    for {
      dataString <- byteSource
        .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
        .map(_.utf8String)
        .runWith(Sink.seq)

      rawSignals = dataString.mkString.parseJson.convertTo[GoogleLocations].rawSignals
      rawSignalsLogEntries = rawSignals
        .filter(_.position.isDefined)
        .filter(_.position.get.accuracyMeters < 100)
        .map(_.position.get)
        .map(position => {
          val parsedLatLng = parseGoogleLatLng(position.LatLng)
          val dateTime = OffsetDateTime.parse(position.timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

          LogEntityInsert("CoordEntity",
            CoordEntity(
              accuracy = Some(position.accuracyMeters),
              latitude = parsedLatLng._1,
              longitude = parsedLatLng._2,
              source = Some("Google"),
              altitude = None).toJson,
            dateTime.toInstant.toEpochMilli
          )
        })
      rawSignalsInserted <- loggingService.insertLogItems(user, rawSignalsLogEntries)

      semanticSegments = dataString.mkString.parseJson.convertTo[GoogleLocations].semanticSegments
      semanticSegmentsLogEntries = semanticSegments
        .filter(_.timelinePath.isDefined)
        .flatMap(_.timelinePath.get)
        .map(timelinePoint => {
          val parsedLatLng = parseGoogleLatLng(timelinePoint.point)
          val dateTime = OffsetDateTime.parse(timelinePoint.time, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

          LogEntityInsert("CoordEntity",
            CoordEntity(
              accuracy = None,
              latitude = parsedLatLng._1,
              longitude = parsedLatLng._2,
              source = Some("Google"),
              altitude = None).toJson,
            dateTime.toInstant.toEpochMilli
          )
        })
      semanticSegmentsInserted <- loggingService.insertLogItems(user, semanticSegmentsLogEntries)

    } yield rawSignalsInserted + semanticSegmentsInserted

  def importApp(byteSource: StreamSource[ByteString, Any])(implicit user: UserEntity): Future[Int] =
    for {
      dataString <- byteSource
        .via(Framing.delimiter(ByteString("}{"), maximumFrameLength = 256, allowTruncation = true))
        .map(_.utf8String)
        .runWith(Sink.seq)

      jsonString = "[" + dataString.mkString("},{") + "]"

      locations = jsonString.parseJson.convertTo[Seq[AppLocation]]
      logEntries = locations.filter(_.data.accuracy.forall(_ < 100)).map(location =>
        LogEntityInsert("CoordEntity", location.data.toJson, location.createdAtClient))

      inserted <- loggingService.insertLogItems(user, logEntries)
    } yield inserted

  def importSamsung(file: File)(implicit user: UserEntity): Future[Int] = {
    import scala.jdk.CollectionConverters._

    val locations: Seq[SamsungLocation] = ZipArchive.fromFile(file).allDirs.asScala
      .foldLeft(List.empty[SamsungLocation]) { case (acc, (_, files)) =>
        acc ::: files.filter(f => f.name.endsWith("location_data.json")).flatMap { f =>
          val string = IoSource.fromInputStream(f.input).mkString
          string.parseJson.convertTo[List[SamsungLocation]]
        }.toList
      }

    val logEntries = locations.map(location => LogEntityInsert("CoordEntity",
      CoordEntity(
        altitude = location.altitude,
        latitude = location.latitude,
        longitude = location.longitude,
        source = Some("Samsung"),
        accuracy = None).toJson, location.start_time))

    loggingService.insertLogItems(user, logEntries)
  }

}
