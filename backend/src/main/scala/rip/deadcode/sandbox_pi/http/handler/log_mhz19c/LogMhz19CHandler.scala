package rip.deadcode.sandbox_pi.http.handler.log_mhz19c

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
class LogMhz19CHandler @Inject() (processData: ProcessData) extends HttpHandler {

  override def url: Regex = "^/log/mhz19c$".r

  override def method: String = "POST"

  override def handle(request: Request): IO[HttpResponse] = {
    for {
      input <- IO.fromEither {
        val inputStr = CharStreams.toString(request.getReader)
        import io.circe.syntax.*
        for {
          json <- io.circe.parser.parse(inputStr)
          input <- json.as[LogMhz19CInput]
        } yield input
      }
      _ <- validateInput(input) match {
        case Validated.Valid(_)   => IO.unit
        case Validated.Invalid(e) => IO.raiseError(???)
      }

      _ <- processData.run(input)
    } yield NoContentHttpResponse()
  }

  private def validateInput(input: LogMhz19CInput): ValidatedNel[String, LogMhz19CInput] = {
    // TODO
    Validated.validNel(input)
  }
}
