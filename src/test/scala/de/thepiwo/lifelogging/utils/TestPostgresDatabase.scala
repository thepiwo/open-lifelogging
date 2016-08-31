package de.thepiwo.lifelogging.utils

import com.typesafe.config.ConfigFactory
import de.thepiwo.lifelogging.restapi.utils.FlywayService

object TestPostgresDatabase {

  private val config = ConfigFactory.load()
  private val testDatabaseConfig = config.getConfig("testdatabase")

  val jdbcUrl = testDatabaseConfig.getString("url")
  val dbUser = testDatabaseConfig.getString("user")
  val dbPassword = testDatabaseConfig.getString("password")


  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  flywayService.dropDatabase.migrateDatabaseSchema
}