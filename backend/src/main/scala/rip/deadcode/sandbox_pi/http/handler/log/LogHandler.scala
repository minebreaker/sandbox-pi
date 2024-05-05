package rip.deadcode.sandbox_pi.http.handler.log

import cats.data.{Validated, ValidatedNel}
import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.handler.log.LogHandler.ValidTables
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}
import rip.deadcode.sandbox_pi.lib.circe.parseJson

import java.io.InputStreamReader
import scala.util.matching.compat.Regex

@Singleton
class LogHandler @Inject() (
    persistData: PersistData
) extends HttpHandler {

  override def url: Regex = "^/log$".r

  override def method: String = "POST"

  override def handle(request: Request): IO[HttpResponse] = {

    for {
      input <- parseJson[LogInput](request)
      _ <- validateInput(input) match {
        case Validated.Valid(_)   => IO.unit
        case Validated.Invalid(e) => IO.raiseError(???)
      }

      _ <- persistData.persist(input)
    } yield JsonHttpResponse(
      200,
      LogOutput("OK")
    )
  }

  private def validateInput(input: LogInput): ValidatedNel[String, LogInput] = {
    (
      Seq(
        Validated.condNel(input.items.length <= 20, (), "items")
      ) ++ input.items.map(i =>
        Validated.condNel(ValidTables.contains(i.target), (), "items.target") combine
          Validated.fromOption(i.value.toDoubleOption, "items.value").map(_ => ()).toValidatedNel
      )
    ).reduce((x, y) => x combine y).map(_ => input)
  }
}

object LogHandler {
  private val ValidTables = Set("temperature", "pressure", "humidity", "co2")
}
