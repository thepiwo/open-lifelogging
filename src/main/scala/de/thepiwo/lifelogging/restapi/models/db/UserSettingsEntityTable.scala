package de.thepiwo.lifelogging.restapi.models.db

import de.thepiwo.lifelogging.restapi.models.UserSettingsEntity
import de.thepiwo.lifelogging.restapi.utils.DatabaseService

trait UserSettingsEntityTable {

  protected val databaseService: DatabaseService

  import databaseService.driver.api._

  class UserSettings(tag: Tag) extends Table[UserSettingsEntity](tag, "user_settings") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Option[Long]]("user_id")

    def userFk = foreignKey("LOG_USER_FK", userId, userSettings)(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def lastFmUsername = column[Option[String]]("last_fm_username")

    def * = (id, userId, lastFmUsername) <> ((UserSettingsEntity.apply _).tupled, UserSettingsEntity.unapply)
  }

  protected val userSettings = TableQuery[UserSettings]

}
