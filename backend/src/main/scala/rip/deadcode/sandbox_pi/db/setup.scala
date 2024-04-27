package rip.deadcode.sandbox_pi.db

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import rip.deadcode.sandbox_pi.lib.jdbi.{ConstructorRowMapperFactoryDelegator, OptionColumnMapperFactory}
import rip.deadcode.sandbox_pi.{Config, DatabaseConfig}

import javax.sql.DataSource
import scala.util.chaining.scalaUtilChainingOps

def createDataSource(config: DatabaseConfig): DataSource = {
  // Should use sttp uri builder?
  val jdbcUrl = s"jdbc:postgresql://${config.host}:${config.port}/${config.database}"

  val hikariConfig = HikariConfig()
    .tap(_.setJdbcUrl(jdbcUrl))
    .tap(_.setUsername(config.username))
    .tap(_.setPassword(config.password))
  HikariDataSource(hikariConfig)
}

def setupFlyway(dataSource: DataSource, config: DatabaseConfig): Unit = {
  val flyway = Flyway
    .configure()
    .cleanDisabled(!config.cleanDatabaseOnStartup)
    .dataSource(dataSource)
    .load()
  if (config.cleanDatabaseOnStartup) {
    flyway.clean()
  }
  flyway.migrate()
}

def createJdbi(dataSource: DataSource): Jdbi = {
  Jdbi
    .create(dataSource)
    .registerRowMapper(new ConstructorRowMapperFactoryDelegator())
    .registerColumnMapper(new OptionColumnMapperFactory())
}
