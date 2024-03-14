package rip.deadcode.sandbox_pi.http.handler.led

import cats.effect.IO
import com.squareup.moshi.Moshi
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.AppContext
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.handler.helloworld.HelloWorldOutput
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import scala.util.matching.compat.Regex

class LedHandler(ctx: AppContext)(using moshi: Moshi) extends HttpHandler {

  override def url: Regex = "^/led$".r

  override def method: String = "GET"

  private val led = new Led(ctx.pi4j)

  override def handle(request: Request, ctx: AppContext): IO[HttpResponse] = {
    for {
      _ <- led.run()
    } yield JsonHttpResponse(
      200,
      HelloWorldOutput("OK")
    )
  }
}
