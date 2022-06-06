package de.thepiwo.lifelogging.restapi.services

import akka.stream.Materializer
import akka.stream.scaladsl.{Framing, Sink, Source => StreamSource}
import akka.util.ByteString
import de.thepiwo.lifelogging.restapi.models.db.TokenEntityTable
import de.thepiwo.lifelogging.restapi.models.{AppLocation, _}
import de.thepiwo.lifelogging.restapi.utils.DatabaseService
import spray.json.{NullOptions, RootJsonFormat, _}

import java.io.File
import scala.concurrent.{ExecutionContext, Future}
import scala.io.{Source => IoSource}
import scala.languageFeature.existentials
import scala.reflect.io.ZipArchive

object ImportJsonProtocol extends DefaultJsonProtocol with NullOptions {
  implicit val googleLocationFormat: RootJsonFormat[GoogleLocation] = jsonFormat4(GoogleLocation)
  implicit val googleLocationsFormat: RootJsonFormat[GoogleLocations] = jsonFormat1(GoogleLocations)
  implicit val coordEntityFormat: RootJsonFormat[CoordEntity] = jsonFormat5(CoordEntity)
  implicit val samsungLocationFormat: RootJsonFormat[SamsungLocation] = jsonFormat4(SamsungLocation)
  implicit val appLocationFormat: RootJsonFormat[AppLocation] = jsonFormat2(AppLocation)
}

class ImportService(val databaseService: DatabaseService, val loggingService: LoggingService)
                   (implicit val executionContext: ExecutionContext, implicit val materializer: Materializer) extends TokenEntityTable {

  import ImportJsonProtocol._

  def importGoogle(byteSource: StreamSource[ByteString, Any])(implicit user: UserEntity): Future[Int] =
    for {
      dataString <- byteSource
        .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
        .map(_.utf8String)
        .runWith(Sink.seq)

      locations = dataString.mkString.parseJson.convertTo[GoogleLocations].locations
      logEntries = locations.filter(_.accuracy < 100).map(location => LogEntityInsert("CoordEntity",
        CoordEntity(
          accuracy = Some(location.accuracy),
          latitude = location.latitudeE7 / 10000000,
          longitude = location.longitudeE7 / 10000000,
          source = Some("Google"),
          altitude = None).toJson, location.timestampMs.toLong))

      inserted <- loggingService.insertLogItems(user, logEntries)
    } yield inserted

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
