package rip.deadcode.sandbox_pi.http.handler.log

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class LogOutput(
    status: String
)

object LogOutput {
  implicit val encoder: Encoder[LogOutput] = deriveEncoder
}
