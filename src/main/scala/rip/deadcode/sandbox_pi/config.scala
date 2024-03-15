package rip.deadcode.sandbox_pi

import com.typesafe.config.{ConfigFactory, Config as TypeSafeConfig}

case class Config(
    port: Int,
    injector: String
)

def readConfig(): Config = {
  val root = ConfigFactory.load()
  val app = root.getConfig("application")
  Config(
    port = app.getInt("port"),
    injector = app.getString("injector")
  )
}
