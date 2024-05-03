package rip.deadcode.sandbox_pi.http.handler.stat

import cats.data.Validated
import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.handler.history.Reader
import rip.deadcode.sandbox_pi.http.handler.stat.StatException.InvalidParameter
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}
import rip.deadcode.sandbox_pi.lib.Validations.*

import scala.util.matching.compat.Regex

@Singleton
class StatHandler @Inject() (reader: Reader) extends HttpHandler {

  override def url: Regex = "^/stat(\\?.+)?$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {

    val roomIdParam = Option(request.getParameter("room_id"))
    val patternParam = Option(request.getParameter("p"))

    val f = for {
      roomId <- validateUuid(roomIdParam, "room_id") match {
        case Validated.Valid(value) => IO(value)
        case Validated.Invalid(e)   => IO.raiseError(InvalidParameter(e.head))
      }
      result <- patternParam match {
        case Some("hour")  => IO.blocking { reader.readHour(roomId) }
        case Some("day")   => IO.blocking { reader.readDay(roomId) }
        case Some("7days") => IO.blocking { reader.read7Days(roomId) }
        case Some("month") => IO.blocking { reader.readMonth(roomId) }
        case _             => IO.raiseError(InvalidParameter("p"))
      }
    } yield JsonHttpResponse(
      200,
      result
    )
    f.recover { case e: InvalidParameter =>
      JsonHttpResponse.invalidParameter(e.param)
    }
  }
}
