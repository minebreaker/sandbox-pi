package rip.deadcode.sandbox_pi.http.handler.stat

sealed abstract class StatException extends RuntimeException

object StatException {
  final case class InvalidParameter(param: String) extends StatException
}
