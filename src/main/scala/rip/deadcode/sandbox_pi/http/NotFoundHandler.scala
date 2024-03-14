package rip.deadcode.sandbox_pi.http

import cats.effect.IO
import com.google.common.net.MediaType
import com.squareup.moshi.Moshi
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.AppContext
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.NotFoundHandler.Response
import rip.deadcode.sandbox_pi.http.handler.helloworld.HelloWorldOutput
import rip.deadcode.sandbox_pi.json.JsonEncode

import scala.util.matching.compat.Regex

class NotFoundHandler(using moshi: Moshi) extends HttpHandler {

  override def url: Regex = "^.*$".r
  override def method: String = "GET"
  override def handle(request: Request, ctx: AppContext): IO[HttpResponse] = IO.pure(
    JsonHttpResponse(
      status = 404,
      body = Response("Not found.")
    )
  )
}

object NotFoundHandler {
  case class Response(message: String)

  given (using moshi: Moshi): JsonEncode[Response] with {
    extension (self: Response) {
      override def encode(): String = moshi.adapter(classOf[Response]).toJson(self)
    }
  }
}
