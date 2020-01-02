package de.thepiwo.lifelogging.restapi.connector.lastfm

import java.sql.Timestamp
import java.time.ZoneOffset

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}
import spray.json._

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object LastFmJsonProtocol extends DefaultJsonProtocol with NullOptions {
  implicit val lastFmDateFormat: RootJsonFormat[LastFmDate] = jsonFormat1(LastFmDate)
  implicit val lastFmAlbumFormat: RootJsonFormat[LastFmAlbum] = jsonFormat2(LastFmAlbum)
  implicit val lastFmArtistFormat: RootJsonFormat[LastFmArtist] = jsonFormat2(LastFmArtist)
  implicit val lastFmTrackFormat: RootJsonFormat[LastFmTrack] = jsonFormat5(LastFmTrack)
  implicit val lastFmAttrFormat: RootJsonFormat[LastFmAttr] = jsonFormat5(LastFmAttr)
  implicit val lastFmRecentTracksFormat: RootJsonFormat[LastFmRecentTracks] = jsonFormat2(LastFmRecentTracks)
  implicit val lastFmRecentTrackBaseFormat: RootJsonFormat[LastFmRecentTrackBase] = jsonFormat1(LastFmRecentTrackBase)
}

object LastFm {

  private val config: Config = ConfigFactory.load()
  private val API_KEY: String = config.getString("connector.lastfm.apikey")
  private val LIMIT: Int = config.getInt("connector.lastfm.limit")
  private val PARALLEL_PAGES: Int = config.getInt("connector.lastfm.parallelpages")
  private val URL: String = config.getString("connector.lastfm.url")

  val log: Logger = LoggerFactory.getLogger("LastFm")
  private implicit val system: ActorSystem = ActorSystem()

  import LastFmJsonProtocol._

  def requestLastFm[T: JsonFormat](method: HttpMethod, uri: Uri, headers: immutable.Seq[HttpHeader]): Future[T] = {
    val request = HttpRequest(method = method, uri = uri, headers = headers)

    implicit val unmarshaller: Unmarshaller[HttpEntity, T] = Unmarshaller
      .stringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .map(_.parseJson.convertTo[T])

    Http(system)
      .singleRequest(request)
      .flatMap(response => Unmarshal(response.entity).to[T])
  }

  def RecentTracks(username: String, fromDate: Option[Timestamp]): Future[Seq[LastFmTrack]] = {
    RecentTracks(username, fromDate, 1) flatMap { result =>
      val fromPage = result.recenttracks.`@attr`.totalPages.toInt;
      val startPage = Math.max(result.recenttracks.`@attr`.totalPages.toInt - PARALLEL_PAGES, 1)

      Future.sequence((startPage to fromPage)
        .map(page => RecentTracks(username, fromDate, page)))
        .map(_.flatten((_.recenttracks.track)))
    }
  }

  def RecentTracks(username: String, fromDate: Option[Timestamp], page: Int): Future[LastFmRecentTrackBase] = {
    log.debug(s"RecentTracks $username $fromDate $page")

    val baseQuery = Map("page" -> page.toString, "user" -> username, "api_key" -> API_KEY, "format" -> "json", "method" -> "user.getrecenttracks", "limit" -> LIMIT.toString)
    val query = fromDate match {
      case None => Query(baseQuery)
      case Some(from) => Query(baseQuery ++ Map("from" -> (from.toLocalDateTime.atOffset(ZoneOffset.UTC).toEpochSecond + 1).toString))
    }

    val uri = Uri(URL).withQuery(query)
    requestLastFm[LastFmRecentTrackBase](HttpMethods.GET, uri, immutable.Seq.empty)
  }
}
