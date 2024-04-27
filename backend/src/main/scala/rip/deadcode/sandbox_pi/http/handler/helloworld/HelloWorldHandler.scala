package rip.deadcode.sandbox_pi.http.handler.helloworld

import cats.effect.IO
import com.google.common.net.MediaType
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import scala.util.matching.compat.Regex

@Singleton
class HelloWorldHandler @Inject() () extends HttpHandler {

  override def url: Regex = "^/(hello|health)$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = IO.pure(
    JsonHttpResponse(
      200,
      HelloWorldOutput("OK")
    )
  )
}
