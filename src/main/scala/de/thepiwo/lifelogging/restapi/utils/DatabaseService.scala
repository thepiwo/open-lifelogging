package de.thepiwo.lifelogging.restapi.utils

import com.github.tminglei.slickpg.PgSprayJsonSupport
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import slick.jdbc.PostgresProfile

class DatabaseService(jdbcUrl: String, dbUser: String, dbPassword: String) {

  case class JBean(name: String, count: Int)

  trait JsonSupportedPostgresDriver extends PostgresProfile
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

  val driver: JsonSupportedPostgresDriver.type = JsonSupportedPostgresDriver
  val db: JsonSupportedPostgresDriver.backend.DatabaseDef = Database.forDataSource(dataSource, None)
  db.createSession()

}
