package rip.deadcode.sandbox_pi

import com.pi4j.context.Context as Pi4JContext

trait AppContext {
  val config: Config
  val pi4j: Pi4JContext
}

case class AppContextImpl(
    config: Config,
    pi4j: Pi4JContext
) extends AppContext
