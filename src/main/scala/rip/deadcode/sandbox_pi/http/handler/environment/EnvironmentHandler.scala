package rip.deadcode.sandbox_pi.http.handler.environment

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}
import rip.deadcode.sandbox_pi.pi.bm680.{Bme680, Bme680Output}
import rip.deadcode.sandbox_pi.pi.mhz19c.{Mhz19c, Mhz19cOutput}
import rip.deadcode.sandbox_pi.utils.{formatCo2, formatHumidity, formatPressure, formatTemperature}

import java.time.format.DateTimeFormatter
import java.time.{Clock, ZoneId, ZonedDateTime}
import scala.util.matching.compat.Regex

@Singleton
class EnvironmentHandler @Inject() (
    bme680: Bme680,
    mhz19c: Mhz19c,
    clock: Clock
) extends HttpHandler {

  override def url: Regex = "^/environment$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {
    IO.blocking {
      val Bme680Output(temp, press, hum, timestamp) = bme680.getData
      val timestampStr = DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.ofInstant(timestamp, clock.getZone))
      val Mhz19cOutput(co2, co2Timestamp) = mhz19c.getData
      JsonHttpResponse(
        200,
        EnvironmentOutput(
          temperature = formatTemperature(temp),
          temperatureRaw = temp,
          temperatureLastUpdate = timestampStr,
          pressure = formatPressure(press),
          pressureRaw = press,
          pressureLastUpdate = timestampStr,
          humidity = formatHumidity(hum),
          humidityRaw = hum,
          humidityLastUpdate = timestampStr,
          co2 = formatCo2(co2),
          co2raw = co2,
          co2LastUpdate = DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.ofInstant(co2Timestamp, clock.getZone))
        )
      )
    }
  }
}
