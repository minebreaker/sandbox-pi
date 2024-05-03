package rip.deadcode.sandbox_pi.http.handler.logger

import cats.data.{Validated, ValidatedNel}
import cats.effect.IO
import com.google.common.io.CharStreams
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.NoContentHttpResponse
import rip.deadcode.sandbox_pi.http.handler.log.LogInput
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import scala.util.matching.compat.Regex

@Singleton
class LoggerHandler @Inject() (persistData: PersistData) extends HttpHandler {

  override def url: Regex = "^/logger$".r

  override def method: String = "POST"

  override def handle(request: Request): IO[HttpResponse] = {
    for {
      input <- IO.fromEither {
        val inputStr = CharStreams.toString(request.getReader)
        import io.circe.syntax.*
        for {
          json <- io.circe.parser.parse(inputStr)
          input <- json.as[LoggerInput]
        } yield input
      }
      _ <- validateInput(input) match {
        case Validated.Valid(a)   => IO.unit
        case Validated.Invalid(e) => IO.raiseError(???)
      }

      _ <- persistData.write(input)
    } yield NoContentHttpResponse()
  }

  private def validateInput(input: LoggerInput): ValidatedNel[String, LoggerInput] = {
    (
      Validated.condNel(!input.deviceId.isBlank, "", "device_id") combine
        Validated.condNel(input.level == "WARN" || input.level == "ERROR" || input.level == "INFO", "", "level")
    ).map(_ => input)
  }
}
