package rip.deadcode.sandbox_pi.http

import com.google.inject.{Inject, Singleton}
import rip.deadcode.sandbox_pi.http.handler.arp.ArpHandler
import rip.deadcode.sandbox_pi.http.handler.arp_check.ArpCheckHandler
import rip.deadcode.sandbox_pi.http.handler.environment.EnvironmentHandler
import rip.deadcode.sandbox_pi.http.handler.helloworld.HelloWorldHandler
import rip.deadcode.sandbox_pi.http.handler.history.HistoryHandler
import rip.deadcode.sandbox_pi.http.handler.led.LedHandler
import rip.deadcode.sandbox_pi.http.handler.log.LogHandler
import rip.deadcode.sandbox_pi.http.handler.log_bme680.LogBme680Handler
import rip.deadcode.sandbox_pi.http.handler.log_mhz19c.LogMhz19CHandler
import rip.deadcode.sandbox_pi.http.handler.log_tgs8100.LogTgs8100Handler
import rip.deadcode.sandbox_pi.http.handler.logger.LoggerHandler
import rip.deadcode.sandbox_pi.http.handler.pi_temperature.PiTemperatureHandler
import rip.deadcode.sandbox_pi.http.handler.room.ListRoomHandler
import rip.deadcode.sandbox_pi.http.handler.stat.StatHandler
import rip.deadcode.sandbox_pi.http.handler.status.StatusHandler
import rip.deadcode.sandbox_pi.http.handler.ui.UiHandler
import rip.deadcode.sandbox_pi.http.handler.ui_classic.UiClassicHandler

@Singleton
class Handlers @Inject() (
    helloWorldHandler: HelloWorldHandler,
    statusHandler: StatusHandler,

    // Hardware related
    ledHandler: LedHandler,
    logHandler: LogHandler,
    logBme680Handler: LogBme680Handler,
    logMhz19CHandler: LogMhz19CHandler,
    logTgs8100Handler: LogTgs8100Handler,
    environmentHandler: EnvironmentHandler,
    historyHandler: HistoryHandler,
    statHandler: StatHandler,
    piTemperatureHandler: PiTemperatureHandler,
    arpHandler: ArpHandler,
    arpCheckHandler: ArpCheckHandler,
    loggerHandler: LoggerHandler,

    // UI
    listRoomHandler: ListRoomHandler,
    uiHandler: UiHandler,
    uiClassicHandler: UiClassicHandler
) {

  val handlers: Seq[HttpHandler] = Seq(
    helloWorldHandler,
    statusHandler,
    ledHandler,
    logHandler,
    logBme680Handler,
    logMhz19CHandler,
    logTgs8100Handler,
    environmentHandler,
    historyHandler,
    statHandler,
    piTemperatureHandler,
    arpHandler,
    arpCheckHandler,
    loggerHandler,
    listRoomHandler,
    uiHandler,
    uiClassicHandler
  )
}
