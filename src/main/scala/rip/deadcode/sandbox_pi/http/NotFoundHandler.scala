package rip.deadcode.sandbox_pi.http

import cats.effect.IO
import com.google.common.net.MediaType
import com.google.inject.{Inject, Singleton}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.NotFoundHandler.Response
import rip.deadcode.sandbox_pi.http.handler.helloworld.HelloWorldOutput

import scala.util.matching.compat.Regex

@Singleton
class NotFoundHandler @Inject() () extends HttpHandler {

  override def url: Regex = "^.*$".r
  override def method: String = "GET"
  override def handle(request: Request): IO[HttpResponse] = IO.pure(
    JsonHttpResponse(
      status = 404,
      body = Response("Not found.")
    )
  )
}

object NotFoundHandler {
  case class Response(message: String)

  object Response {
    implicit val encoder: Encoder[Response] = deriveEncoder
  }
}
