package rip.deadcode.sandbox_pi

import com.google.inject.{Binder, Module}
import com.pi4j.context.Context as Pi4JContext
import org.jdbi.v3.core.Jdbi

import java.time.{Clock, ZoneId}

class PiModule(
    config: Config,
    pi4j: Pi4JContext,
    jdbi: Jdbi
) extends Module {

  override def configure(binder: Binder): Unit = {
    binder.bind(classOf[Clock]).toInstance(Clock.systemDefaultZone())
    binder.bind(classOf[Config]).toInstance(config)
    binder.bind(classOf[Pi4JContext]).toInstance(pi4j)
    binder.bind(classOf[Jdbi]).toInstance(jdbi)
  }
}
