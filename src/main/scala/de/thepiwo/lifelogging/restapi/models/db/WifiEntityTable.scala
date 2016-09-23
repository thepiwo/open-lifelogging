package de.thepiwo.lifelogging.restapi.models.db

import de.thepiwo.lifelogging.restapi.models.WifiEntity
import de.thepiwo.lifelogging.restapi.utils.DatabaseService

trait WifiEntityTable extends LogEntityTable {

  protected val databaseService: DatabaseService

  import databaseService.driver.api._

  class Wifi(tag: Tag) extends Table[WifiEntity](tag, "log_wifi") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def logEntityId = column[Option[Long]]("log_entity_id")

    def logEntityFk = foreignKey("LOG_ENTITY_FK", logEntityId, logs)(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def ssid = column[String]("ssid")

    def speed = column[Int]("speed")

    def status = column[String]("status")


    def * = (id, logEntityId, ssid, speed, status) <> ((WifiEntity.apply _).tupled, WifiEntity.unapply)

  }

  protected val logWifi = TableQuery[Wifi]

}
