package rip.deadcode.sandbox_pi.http.handler.environment

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import com.squareup.moshi.Moshi
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}
import rip.deadcode.sandbox_pi.pi.bm680.{Bme680, Bme680Output}
import rip.deadcode.sandbox_pi.pi.mhz19c.{Mhz19c, Mhz19cOutput}

import java.time.format.DateTimeFormatter
import java.time.{Clock, ZoneId, ZonedDateTime}
import scala.util.matching.compat.Regex

@Singleton
class EnvironmentHandler @Inject() (
    bme680: Bme680,
    mhz19c: Mhz19c,
    clock: Clock
)(using moshi: Moshi)
    extends HttpHandler {

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
          temperature = String.format("%.1f ℃", temp),
          temperatureRaw = temp,
          temperatureLastUpdate = timestampStr,
          pressure = String.format("%f ㍱", press / 100),
          pressureRaw = press,
          pressureLastUpdate = timestampStr,
          humidity = String.format("%.1f %%", hum),
          humidityRaw = hum,
          humidityLastUpdate = timestampStr,
          co2 = s"$co2 ㏙",
          co2raw = co2,
          co2LastUpdate = DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.ofInstant(co2Timestamp, clock.getZone))
        )
      )
    }
  }
}
