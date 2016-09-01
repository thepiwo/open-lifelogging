package de.thepiwo.lifelogging.restapi.models.db

import java.sql.Timestamp

import de.thepiwo.lifelogging.restapi.models.LogEntity
import de.thepiwo.lifelogging.restapi.utils.DatabaseService

trait LogEntityTable extends UserEntityTable {

  protected val databaseService: DatabaseService

  import databaseService.driver.api._

  class Logs(tag: Tag) extends Table[LogEntity](tag, "logs") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Option[Long]]("user_id")

    def userFk = foreignKey("LOG_USER_FK", userId, users)(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def key = column[String]("key")

    def createdAt = column[Timestamp]("created_at")

    def * = (id, userId, key, createdAt) <> ((LogEntity.apply _).tupled, LogEntity.unapply)

  }

  protected val logs = TableQuery[Logs]

}