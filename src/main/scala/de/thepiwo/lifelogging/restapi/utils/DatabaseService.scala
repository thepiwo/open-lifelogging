package de.thepiwo.lifelogging.restapi.utils

import com.github.tminglei.slickpg.PgSprayJsonSupport
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

class DatabaseService(jdbcUrl: String, dbUser: String, dbPassword: String) {

  import slick.driver.PostgresDriver

  case class JBean(name: String, count: Int)

  trait JsonSupportedPostgresDriver extends PostgresDriver
    with PgSprayJsonSupport {
    override val pgjson = "jsonb"

    override val api = new API with SprayJsonImplicits
  }

  object JsonSupportedPostgresDriver extends JsonSupportedPostgresDriver


  private val hikariConfig = new HikariConfig()
  hikariConfig.setJdbcUrl(jdbcUrl)
  hikariConfig.setUsername(dbUser)
  hikariConfig.setPassword(dbPassword)

  private val dataSource = new HikariDataSource(hikariConfig)

  import JsonSupportedPostgresDriver.api._

  val driver = JsonSupportedPostgresDriver
  val db = Database.forDataSource(dataSource)
  db.createSession()

}
