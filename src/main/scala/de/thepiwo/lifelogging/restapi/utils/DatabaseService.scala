package de.thepiwo.lifelogging.restapi.utils

import com.github.tminglei.slickpg.{ExPostgresProfile, PgDateSupport, PgSprayJsonSupport}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

trait JsonSupportedPostgresDriver extends ExPostgresProfile with PgSprayJsonSupport with PgDateSupport {
  override val pgjson = "jsonb"

  override val api = new API with SprayJsonImplicits with DateTimeImplicits

  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate
}

object JsonSupportedPostgresDriver extends JsonSupportedPostgresDriver

class DatabaseService(jdbcUrl: String, dbUser: String, dbPassword: String) {

  case class JBean(name: String, count: Int)

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
