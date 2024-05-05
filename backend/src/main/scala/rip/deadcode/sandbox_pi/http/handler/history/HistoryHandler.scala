package rip.deadcode.sandbox_pi.http.handler.history

import cats.data.Validated
import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.handler.history.HistoryException.InvalidParameter
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}
import rip.deadcode.sandbox_pi.lib.cats.Validations.*

import java.util.UUID
import scala.util.matching.compat.Regex

@Singleton
class HistoryHandler @Inject() (reader: Reader) extends HttpHandler {

  override def url: Regex = "^/history(\\?.+)?$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {

    val roomIdParam = Option(request.getParameter("room_id"))
    val yearParam = Option(request.getParameter("y"))
    val monthParam = Option(request.getParameter("m"))
    val dayParam = Option(request.getParameter("d"))
    val hourParam = Option(request.getParameter("h"))

    import cats.syntax.all.*
    val f = for {
      result <- (
        validateUuid(roomIdParam, "room_id"),
        validateOptionInt(yearParam, "year"),
        validateOptionInt(monthParam, "month"),
        validateOptionInt(dayParam, "day"),
        validateOptionInt(hourParam, "hour")
      ).mapN((rid, y, m, d, h) => (rid, y, m, d, h)) match {
        case Validated.Valid(v) =>
          v match {
            case (rid, Some(y), Some(m), None, None)       => IO.blocking { reader.readMonth(rid, y, m) }
            case (rid, Some(y), Some(m), Some(d), None)    => IO.blocking { reader.readDay(rid, y, m, d) }
            case (rid, Some(y), Some(m), Some(d), Some(h)) => IO.blocking { reader.readHour(rid, y, m, d, h) }
            case _                                         => IO.raiseError(InvalidParameter("y, m, d, h"))
          }
        case Validated.Invalid(e) => IO.raiseError(InvalidParameter(e.mkString_(", ")))
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
