package rip.deadcode.sandbox_pi.http

import com.google.inject.{Inject, Singleton}
import rip.deadcode.sandbox_pi.http.handler.environment.EnvironmentHandler
import rip.deadcode.sandbox_pi.http.handler.helloworld.HelloWorldHandler
import rip.deadcode.sandbox_pi.http.handler.history.HistoryHandler
import rip.deadcode.sandbox_pi.http.handler.led.LedHandler
import rip.deadcode.sandbox_pi.http.handler.pi_temperature.PiTemperatureHandler
import rip.deadcode.sandbox_pi.http.handler.stat.StatHandler
import rip.deadcode.sandbox_pi.http.handler.ui.UiHandler

@Singleton
class Handlers @Inject() (
    helloWorldHandler: HelloWorldHandler,
    ledHandler: LedHandler,
    piTemperatureHandler: PiTemperatureHandler,
    environmentHandler: EnvironmentHandler,
    historyHandler: HistoryHandler,
    statHandler: StatHandler,
    uiHandler: UiHandler
) {

  val handlers: Seq[HttpHandler] = Seq(
    helloWorldHandler,
    ledHandler,
    piTemperatureHandler,
    environmentHandler,
    historyHandler,
    statHandler,
    uiHandler
  )
}
