package de.thepiwo.lifelogging.restapi.models.db

import slick.sql.SqlProfile.ColumnOption.SqlType
import de.thepiwo.lifelogging.restapi.utils.JsonSupportedPostgresDriver.api._

abstract class IdTable[T](tag: Tag, name: String) extends Table[T](tag, name) {
  def id: Rep[Long] = column[Long]("id", SqlType("SERIAL"), O.PrimaryKey, O.AutoInc)
}