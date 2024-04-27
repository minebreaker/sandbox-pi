package rip.deadcode.sandbox_pi.http.handler.led

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.handler.helloworld.HelloWorldOutput
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import scala.util.matching.compat.Regex

@Singleton
class LedHandler @Inject() (led: Led) extends HttpHandler {

  override def url: Regex = "^/led$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {
    for {
      _ <- led.run()
    } yield JsonHttpResponse(
      200,
      HelloWorldOutput("OK")
    )
  }
}
