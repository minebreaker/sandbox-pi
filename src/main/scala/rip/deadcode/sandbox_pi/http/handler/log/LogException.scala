package rip.deadcode.sandbox_pi.http.handler.log

sealed abstract class LogException extends RuntimeException

object LogException {
  final case class InvalidParameter(param: String) extends LogException
}
