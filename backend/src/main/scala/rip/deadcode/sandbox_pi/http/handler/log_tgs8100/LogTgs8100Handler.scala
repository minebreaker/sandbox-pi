package rip.deadcode.sandbox_pi.http.handler.log_tgs8100

import cats.data.{Validated, ValidatedNel}
import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.NoContentHttpResponse
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}
import rip.deadcode.sandbox_pi.lib.cats.Validations.toIO
import rip.deadcode.sandbox_pi.lib.circe.parseJson

import scala.util.matching.compat.Regex

@Singleton
class LogTgs8100Handler @Inject() (persistData: PersistData) extends HttpHandler {

  override def url: Regex = "^/log/tgs8100$".r

  override def method: String = "POST"

  override def handle(request: Request): IO[HttpResponse] = {
    for {
      input <- parseJson[LogTgs8100Input](request)
      _ <- validateInput(input).toIO

      _ <- persistData.run(input)
    } yield NoContentHttpResponse()
  }

  private def validateInput(input: LogTgs8100Input): ValidatedNel[String, LogTgs8100Input] = {
    Validated.validNel(input)
  }
}
