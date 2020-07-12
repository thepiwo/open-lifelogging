package de.thepiwo.lifelogging.restapi.models.db

import de.thepiwo.lifelogging.restapi.models.TokenEntity
import de.thepiwo.lifelogging.restapi.utils.JsonSupportedPostgresDriver.api._
import slick.collection.heterogeneous.HNil

trait TokenEntityTable extends UserEntityTable {

  class Tokens(tag: Tag) extends IdTable[TokenEntity](tag, "tokens") {
    def userId = column[Long]("user_id")

    def token = column[String]("token")

    def userFk = foreignKey("USER_FK", userId, users)(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def * = (id :: userId :: token :: HNil).mapTo[TokenEntity]
  }

  protected val tokens = TableQuery[Tokens]

}
