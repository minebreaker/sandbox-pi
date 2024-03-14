package rip.deadcode.sandbox_pi.http.handler.temperature

import cats.effect.IO
import com.squareup.moshi.Moshi
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.AppContext
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}
import rip.deadcode.sandbox_pi.pi.bm680.Bme680
import rip.deadcode.sandbox_pi.pi.mhz19c.Mhz19c

import scala.util.matching.compat.Regex

class TemperatureHandler(ctx: AppContext)(using moshi: Moshi) extends HttpHandler {

  override def url: Regex = "^/temp$".r

  override def method: String = "GET"

  private val bme680 = new Bme680(ctx.pi4j)
  private val mhz19c = new Mhz19c(ctx.pi4j)

  override def handle(request: Request, ctx: AppContext): IO[HttpResponse] = {
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
