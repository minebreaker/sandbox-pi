package rip.deadcode.sandbox_pi.http.handler.history

import cats.data.{Validated, ValidatedNec, ValidatedNel}
import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import com.squareup.moshi.Moshi
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.handler.history.HistoryException.InvalidParameter
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import scala.util.matching.compat.Regex

@Singleton
class HistoryHandler @Inject() (reader: Reader)(using moshi: Moshi) extends HttpHandler {

  override def url: Regex = "^/history(\\?.+)?".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {

    val yearParam = Option(request.getParameter("y"))
    val monthParam = Option(request.getParameter("m"))
    val dayParam = Option(request.getParameter("d"))
    val hourParam = Option(request.getParameter("h"))

    import cats.syntax.all.*
    val f = for {
      result <- (
        validateOptionInt(yearParam, "year"),
        validateOptionInt(monthParam, "month"),
        validateOptionInt(dayParam, "day"),
        validateOptionInt(hourParam, "hour")
      ).mapN((y, m, d, h) => (y, m, d, h)) match {
        case Validated.Valid((y, m, d, h)) =>
          (y, m, d, h) match {
            case (Some(y), Some(m), None, None)       => IO.blocking { reader.readMonth(y, m) }
            case (Some(y), Some(m), Some(d), None)    => IO.blocking { reader.readDay(y, m, d) }
            case (Some(y), Some(m), Some(d), Some(h)) => IO.blocking { reader.readHour(y, m, d, h) }
            case _                                    => IO.raiseError(InvalidParameter("y, m, d, h"))
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

  private def validateOptionInt(i: Option[String], param: => String): ValidatedNel[String, Option[Int]] = {
    i match {
      case Some(i) => Validated.fromOption(i.toIntOption, param).map(Some(_)).toValidatedNel
      case None    => Validated.validNel(None)
    }
  }
}
