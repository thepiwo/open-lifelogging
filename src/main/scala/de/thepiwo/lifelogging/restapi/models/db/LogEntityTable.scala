package de.thepiwo.lifelogging.restapi.models.db

import java.sql.Timestamp

import de.thepiwo.lifelogging.restapi.models.LogEntity
import slick.collection.heterogeneous.HNil
import spray.json.JsValue
import de.thepiwo.lifelogging.restapi.utils.JsonSupportedPostgresDriver.api._

trait LogEntityTable extends UserEntityTable {

  class Logs(tag: Tag) extends IdTable[LogEntity](tag, "logs") {
    def userId = column[Long]("user_id")

    def userFk = foreignKey("LOG_USER_FK", userId, users)(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def key = column[String]("key")

    def data = column[JsValue]("data")

    def hash = column[String]("hash")

    def createdAtClient = column[Timestamp]("created_at_client")

    def createdAt = column[Timestamp]("created_at")

    def * = (id :: userId :: key :: data :: hash :: createdAtClient :: createdAt :: HNil).mapTo[LogEntity]
  }

  protected val logs = TableQuery[Logs]

}
