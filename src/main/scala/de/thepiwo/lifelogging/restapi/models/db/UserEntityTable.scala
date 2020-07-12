package de.thepiwo.lifelogging.restapi.models.db

import de.thepiwo.lifelogging.restapi.models.UserEntity
import de.thepiwo.lifelogging.restapi.utils.JsonSupportedPostgresDriver.api._
import slick.collection.heterogeneous.HNil

trait UserEntityTable {

  class Users(tag: Tag) extends IdTable[UserEntity](tag, "users") {
    def username = column[String]("username")

    def password = column[String]("password")

    def * = (id :: username :: password :: HNil).mapTo[UserEntity]
  }

  protected val users = TableQuery[Users]

}
