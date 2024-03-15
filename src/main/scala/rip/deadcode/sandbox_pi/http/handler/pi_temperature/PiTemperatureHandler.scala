package rip.deadcode.sandbox_pi.http.handler.pi_temperature

import cats.effect.IO
import com.squareup.moshi.Moshi
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import scala.util.matching.compat.Regex

class PiTemperatureHandler(using moshi: Moshi) extends HttpHandler {

  override def url: Regex = "^/pi_temp$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {
    for {
      result <- PiTemperature.run()
    } yield HttpResponse.JsonHttpResponse(
      200,
      result
    )
  }
}
