package de.thepiwo.lifelogging.restapi

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import de.thepiwo.lifelogging.restapi.SchedulerMessages.{UpdateLastFm, UpdateLastFmFor}
import de.thepiwo.lifelogging.restapi.connector.LastFm
import de.thepiwo.lifelogging.restapi.models.{LogEntityInsert, UserEntity}
import de.thepiwo.lifelogging.restapi.services.{LoggingService, UsersService}
import org.slf4j.{Logger, LoggerFactory}
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

  system.scheduler.schedule(0 milliseconds, LASTFM_RATE minute, actor, SchedulerMessages.UpdateLastFm)
}

class SchedulerActor(val usersService: UsersService, loggingService: LoggingService)(implicit timeout: Timeout) extends Actor {

  import de.thepiwo.lifelogging.restapi.connector.LastFmJsonProtocol._
  import usersService._

  val log: Logger = LoggerFactory.getLogger("SchedulerActor")

  log.debug("Started SchedulerActor")

  def receive = {
    case UpdateLastFm =>
      getLastFmUsers onComplete {
        case Success(users) =>
          users.foreach { case (username, user) =>
            self ! UpdateLastFmFor(username, user)
          }
        case Failure(e) => e.printStackTrace()
      }

    case UpdateLastFmFor(username, user) =>
      log.debug(s"Received UpdateLastFmFor $username")
      LastFm.RecentTracks(username) onComplete {
        case Success(tracks) =>
          tracks.recenttracks.track.foreach { track =>
            val playingDate = track.date match {
              case Some(date) => date.uts.toLong
              case None => System.currentTimeMillis()
            }
            loggingService.createLogItem(user, LogEntityInsert("LastFMSong", track.toJson, playingDate))
          }
        case Failure(e) => e.printStackTrace()
      }
  }
}