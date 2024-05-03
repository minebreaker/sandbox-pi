package rip.deadcode.sandbox_pi.http.handler.logger

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

import java.time.ZonedDateTime

case class LoggerInput(
    deviceId: String,
    level: String,
    value: String
)

object LoggerInput {
  import rip.deadcode.sandbox_pi.lib.circe.*
  implicit val decoder: Decoder[LoggerInput] = deriveDecoder
}
