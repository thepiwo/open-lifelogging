package de.thepiwo.lifelogging.restapi.models.db

import de.thepiwo.lifelogging.restapi.models.UserSettingsEntity
import slick.collection.heterogeneous.HNil
import de.thepiwo.lifelogging.restapi.utils.JsonSupportedPostgresDriver.api._

trait UserSettingsEntityTable {

  class UserSettings(tag: Tag) extends IdTable[UserSettingsEntity](tag, "user_settings") {
    def userId = column[Long]("user_id")

    def userFk = foreignKey("LOG_USER_FK", userId, userSettings)(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def lastFmUsername = column[Option[String]]("last_fm_username")

    def * = (id :: userId :: lastFmUsername :: HNil).mapTo[UserSettingsEntity]
  }

  protected val userSettings = TableQuery[UserSettings]

}
