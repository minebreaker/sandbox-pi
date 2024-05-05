package rip.deadcode.sandbox_pi.http.handler.log_bme680

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class LogBme680Input(
    deviceId: String,
    roomId: String,
    parT1: String,
    parT2T3: String,
    parP: String,
    parH: String,
    tempAdc: String,
    pressAdc: String,
    humAdc: String
)

object LogBme680Input {

  import rip.deadcode.sandbox_pi.lib.circe.*
  implicit val decoder: Decoder[LogBme680Input] = deriveDecoder
}
