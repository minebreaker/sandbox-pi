package rip.deadcode.sandbox_pi.http.handler.log_bme680

import cats.data.{Validated, ValidatedNel}
import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.NoContentHttpResponse
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}
import rip.deadcode.sandbox_pi.lib.circe.parseJson

import scala.util.matching.compat.Regex

@Singleton
class LogBme680Handler @Inject() (processData: ProcessData) extends HttpHandler {

  override def url: Regex = "^/log/bme680$".r

  override def method: String = "POST"

  override def handle(request: Request): IO[HttpResponse] = {
    for {
      input <- parseJson[LogBme680Input](request)
      _ <- validateInput(input) match {
        case Validated.Valid(_)   => IO.unit
        case Validated.Invalid(e) => IO.raiseError(???)
      }

      _ <- processData.run(input)
    } yield NoContentHttpResponse()
  }

  private def validateInput(input: LogBme680Input): ValidatedNel[String, LogBme680Input] = {
    // TODO
    Validated.validNel(input)
  }
}
