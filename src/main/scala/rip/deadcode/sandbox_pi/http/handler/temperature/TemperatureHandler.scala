package rip.deadcode.sandbox_pi.http.handler.temperature

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import com.squareup.moshi.Moshi
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}
import rip.deadcode.sandbox_pi.pi.bm680.Bme680
import rip.deadcode.sandbox_pi.pi.mhz19c.Mhz19c

import scala.util.matching.compat.Regex

@Singleton
class TemperatureHandler @Inject() (
    bme680: Bme680,
    mhz19c: Mhz19c
)(using moshi: Moshi)
    extends HttpHandler {

  override def url: Regex = "^/temp$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {
    IO.blocking {
      bme680.run()
      mhz19c.run()
      JsonHttpResponse(
        200,
        TemperatureOutput()
      )
    }
  }
}
