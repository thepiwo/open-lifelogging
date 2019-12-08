package de.thepiwo.lifelogging.restapi.utils

import org.flywaydb.core.Flyway

class FlywayService(jdbcUrl: String, dbUser: String, dbPassword: String) {

  private val flyway = Flyway.configure().dataSource(jdbcUrl, dbUser, dbPassword).load()

  def migrateDatabaseSchema: FlywayService = {
    flyway.migrate()
    this
  }

  def dropDatabase: FlywayService = {
    flyway.clean()
    this
  }

}
