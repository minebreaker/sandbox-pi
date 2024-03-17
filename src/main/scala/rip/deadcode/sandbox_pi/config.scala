package rip.deadcode.sandbox_pi

import com.typesafe.config.{ConfigFactory, Config as TypeSafeConfig}

case class Config(
    port: Int,
    injector: String,
    database: DatabaseConfig
)

case class DatabaseConfig(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String,
    cleanDatabaseOnStartup: Boolean
)

def readConfig(): Config = {
  val root = ConfigFactory.load()
  val app = root.getConfig("application")
  val db = root.getConfig("database")
  Config(
    port = app.getInt("port"),
    injector = app.getString("injector"),
    database = DatabaseConfig(
      host = db.getString("host"),
      port = db.getInt("port"),
      username = db.getString("username"),
      password = db.getString("password"),
      database = db.getString("database"),
      cleanDatabaseOnStartup = db.getBoolean("dangerouslyCleanDatabaseOnStartup")
    )
  )
}
