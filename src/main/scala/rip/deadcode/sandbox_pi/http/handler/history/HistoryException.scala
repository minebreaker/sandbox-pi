package rip.deadcode.sandbox_pi.http.handler.history

sealed abstract class HistoryException extends RuntimeException

object HistoryException {
  case class InvalidParameter(param: String) extends HistoryException
}
