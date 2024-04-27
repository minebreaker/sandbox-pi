package rip.deadcode.sandbox_pi

import com.typesafe.config.{ConfigFactory, Config as TypeSafeConfig}
import org.slf4j.Logger

case class Config(
    port: Int,
    injector: String,
    discord: DiscordConfig,
    database: DatabaseConfig
)

case class DiscordConfig(
    webhook: Option[String]
)

case class DatabaseConfig(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String,
    cleanDatabaseOnStartup: Boolean
)

extension (self: TypeSafeConfig) {
  def getStringOpt(path: String): Option[String] = {
    if (self.hasPath(path)) {
      Some(self.getString(path))
    } else {
      None
    }
  }
}

def readConfig(): Config = {
  val root = ConfigFactory.load()
  val app = root.getConfig("application")
  val discord = app.getConfig("discord")
  val db = root.getConfig("database")
  Config(
    port = app.getInt("port"),
    injector = app.getString("injector"),
    discord = DiscordConfig(
      webhook = discord.getStringOpt("webhook")
    ),
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

private def mask(s: String): String =
  if (s.length <= 2) {
    "*****"
  } else {
    s.substring(0, 2) + "*****"
  }

def logConfig(config: Config, logger: Logger): Unit = {
  val maskedConfig = config.copy(
    discord = config.discord.copy(
      webhook = config.discord.webhook.map(mask)
    )
  )
  logger.info("Config: {}", maskedConfig)
}
