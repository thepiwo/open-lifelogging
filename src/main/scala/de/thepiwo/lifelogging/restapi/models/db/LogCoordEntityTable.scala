package de.thepiwo.lifelogging.restapi.models.db

import de.thepiwo.lifelogging.restapi.models.LogCoordEntity
import de.thepiwo.lifelogging.restapi.utils.DatabaseService

trait LogCoordEntityTable extends LogEntityTable {

  protected val databaseService: DatabaseService

  import databaseService.driver.api._

  class LogCoords(tag: Tag) extends Table[LogCoordEntity](tag, "log_coords") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def logEntityId = column[Option[Long]]("log_entity_id")

    def logEntityFk = foreignKey("LOG_ENTITY_FK", logEntityId, logs)(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def latitude = column[Double]("latitude")

    def longitude = column[Double]("longitude")

    def altitude = column[Double]("altitude")

    def accuracy = column[Float]("accuracy")

    def * = (id, logEntityId, latitude, longitude, altitude, accuracy) <> ((LogCoordEntity.apply _).tupled, LogCoordEntity.unapply)

  }

  protected val logCoords = TableQuery[LogCoords]

}
