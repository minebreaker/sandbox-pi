package rip.deadcode.sandbox_pi.http.handler.pi_temperature

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class PiTemperatureOutput(
    raw: Int,
    human: String
)

object PiTemperatureOutput {
  implicit val encoder: Encoder[PiTemperatureOutput] = deriveEncoder
}
