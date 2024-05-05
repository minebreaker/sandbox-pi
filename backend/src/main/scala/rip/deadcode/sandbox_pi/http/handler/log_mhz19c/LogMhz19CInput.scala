package rip.deadcode.sandbox_pi.http.handler.log_mhz19c

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class LogMhz19CInput(
    deviceId: String,
    roomId: String,
    value: String
)

object LogMhz19CInput {

  import rip.deadcode.sandbox_pi.lib.circe.*
  implicit val decoder: Decoder[LogMhz19CInput] = deriveDecoder
}
