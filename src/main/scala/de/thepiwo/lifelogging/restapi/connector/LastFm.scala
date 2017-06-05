package de.thepiwo.lifelogging.restapi.connector

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class LastFmRecentTrackBase(recenttracks: LastFmRecentTracks)

case class LastFmRecentTracks(track: Seq[LastFmTrack],
                              `@attr`: LastFmAttr)

case class LastFmAttr(user: String,
                      page: String,
                      perPage: String,
                      totalPages: String,
                      total: String)

case class LastFmTrack(artist: LastFmArtist,
                       name: String,
                       mbid: String,
                       album: LastFmAlbum,
                       date: Option[LastFmDate])

case class LastFmArtist(`#text`: String,
                        mbid: String)

case class LastFmAlbum(`#text`: String,
                       mbid: String)

case class LastFmDate(uts: String)

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
  private val URL: String = config.getString("connector.lastfm.url")

  private implicit val system = ActorSystem()
  private implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))

  import LastFmJsonProtocol._

  def requestLastFm[T: JsonFormat](method: HttpMethod, uri: Uri, headers: immutable.Seq[HttpHeader]): Future[T] = {
    val request = HttpRequest(
      method = method,
      uri = uri,
      headers = headers
    )

    implicit val unmarshaller = Unmarshaller
      .stringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .map(_.parseJson.convertTo[T])

    Http(system)
      .singleRequest(request)
      .flatMap(response => Unmarshal(response.entity).to[T])
  }

  //TODO pagination, duplicate filter
  def RecentTracks(username: String): Future[LastFmRecentTrackBase] = {
    val uri = Uri(URL).withQuery(Query("user" -> username, "api_key" -> API_KEY, "format" -> "json", "method" -> "user.getrecenttracks"))
    requestLastFm[LastFmRecentTrackBase](HttpMethods.GET, uri, immutable.Seq.empty)
  }
}
