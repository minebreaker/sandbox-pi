package rip.deadcode.sandbox_pi.http.handler.log_tgs8100

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class LogTgs8100Input(
    deviceId: String,
    roomId: String,
    value: String
)

object LogTgs8100Input {

  import rip.deadcode.sandbox_pi.lib.circe.*
  implicit val decoder: Decoder[LogTgs8100Input] = deriveDecoder
}
