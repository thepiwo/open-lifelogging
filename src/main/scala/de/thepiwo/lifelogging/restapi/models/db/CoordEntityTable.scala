package de.thepiwo.lifelogging.restapi.models.db

import de.thepiwo.lifelogging.restapi.models.CoordEntity
import de.thepiwo.lifelogging.restapi.utils.DatabaseService

trait CoordEntityTable extends LogEntityTable {

  protected val databaseService: DatabaseService

  import databaseService.driver.api._

  class Coord(tag: Tag) extends Table[CoordEntity](tag, "log_coord") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def logEntityId = column[Option[Long]]("log_entity_id")

    def logEntityFk = foreignKey("LOG_ENTITY_FK", logEntityId, logs)(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def latitude = column[Double]("latitude")

    def longitude = column[Double]("longitude")

    def altitude = column[Double]("altitude")

    def accuracy = column[Float]("accuracy")

    def * = (id, logEntityId, latitude, longitude, altitude, accuracy) <> ((CoordEntity.apply _).tupled, CoordEntity.unapply)

  }

  protected val logCoord = TableQuery[Coord]

}
