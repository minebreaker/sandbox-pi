package rip.deadcode.sandbox_pi

import com.google.inject.{Binder, Module}
import com.pi4j.context.Context as Pi4JContext
import com.squareup.moshi.Moshi

class PiModule(
    config: Config,
    pi4j: Pi4JContext,
    moshi: Moshi
) extends Module {

  override def configure(binder: Binder): Unit = {
    binder.bind(classOf[Config]).toInstance(config)
    binder.bind(classOf[Pi4JContext]).toInstance(pi4j)
    binder.bind(classOf[Moshi]).toInstance(moshi)
  }
}
