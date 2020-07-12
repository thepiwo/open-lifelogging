package de.thepiwo.lifelogging.restapi.models.db

import de.thepiwo.lifelogging.restapi.models.UserEntity
import de.thepiwo.lifelogging.restapi.utils.DatabaseService
import slick.collection.heterogeneous.HNil

trait UserEntityTable {

  protected val databaseService: DatabaseService

  import databaseService.driver.api._

  class Users(tag: Tag) extends Table[UserEntity](tag, "users") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def username = column[String]("username")

    def password = column[String]("password")

    def * = (id :: username :: password :: HNil).mapTo[UserEntity]
  }

  protected val users = TableQuery[Users]

}
