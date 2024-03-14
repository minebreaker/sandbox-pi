package rip.deadcode.sandbox_pi.http.handler.helloworld

import cats.effect.IO
import com.google.common.net.MediaType
import com.squareup.moshi.Moshi
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.AppContext
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import scala.util.matching.compat.Regex

class HelloWorldHandler(using moshi: Moshi) extends HttpHandler {

  override def url: Regex = "^/health$".r

  override def method: String = "GET"

  override def handle(request: Request, ctx: AppContext): IO[HttpResponse] = IO.pure(
    JsonHttpResponse(
      200,
      HelloWorldOutput("OK")
    )
  )
}
