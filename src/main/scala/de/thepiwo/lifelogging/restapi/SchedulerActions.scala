package de.thepiwo.lifelogging.restapi

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import de.thepiwo.lifelogging.restapi.SchedulerMessages.{UpdateLastFm, UpdateLastFmFor}
import de.thepiwo.lifelogging.restapi.connector.lastfm.LastFm
import de.thepiwo.lifelogging.restapi.models.{LogEntityInsert, UserEntity}
import de.thepiwo.lifelogging.restapi.services.{LoggingService, UsersService}
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object SchedulerMessages {

  case object UpdateLastFm

  case class UpdateLastFmFor(username: String, user: UserEntity)

}

class SchedulerActions(val usersService: UsersService, loggingService: LoggingService) {

  implicit val timeout: FiniteDuration = Timeout(60 seconds).duration

  private val config: Config = ConfigFactory.load()
  private val LASTFM_RATE: Int = config.getInt("connector.lastfm.rate")

  private val system = ActorSystem("SchedulerSystem")
  private val actor = system.actorOf(Props(new SchedulerActor(usersService, loggingService)(timeout)), "SchedulerActor")

  system.scheduler.scheduleAtFixedRate(0 milliseconds, LASTFM_RATE minute, actor, UpdateLastFm)
}

class SchedulerActor(val usersService: UsersService, loggingService: LoggingService)(implicit timeout: Timeout)
  extends Actor with LazyLogging {

  import de.thepiwo.lifelogging.restapi.connector.lastfm.LastFmJsonProtocol._
  import usersService._

  logger.debug("Started SchedulerActor")

  def receive: PartialFunction[Any, Unit] = {
    case UpdateLastFm =>
      getLastFmUsers onComplete {
        case Success(users) =>
          users.foreach { case (username, user) =>
            self ! UpdateLastFmFor(username, user)
          }
        case Failure(e) => e.printStackTrace()
      }

    case UpdateLastFmFor(username, user) =>
      logger.debug(s"Received UpdateLastFmFor $username")
      for {
        latestLogEntity <- loggingService.getLatestLog(user, "LastFMSong")
        tracks <- LastFm.RecentTracks(username, latestLogEntity.map(_.createdAtClient))
        _ = logger.debug(s"UpdateLastFmFor ${tracks.length} tracks")
        logEntries = tracks
          .filter(_.date.isDefined)
          .map(track => LogEntityInsert("LastFMSong", track.toJson, track.date.get.uts.toLong * 1000))
        inserted <- loggingService.insertLogItems(user, logEntries)
      } yield inserted
  }
}