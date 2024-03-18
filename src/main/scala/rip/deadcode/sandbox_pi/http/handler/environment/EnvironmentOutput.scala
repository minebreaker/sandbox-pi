package rip.deadcode.sandbox_pi.http.handler.environment

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class EnvironmentOutput(
    temperature: String,
    temperatureRaw: Double,
    temperatureLastUpdate: String,
    pressure: String,
    pressureRaw: Double,
    pressureLastUpdate: String,
    humidity: String,
    humidityRaw: Double,
    humidityLastUpdate: String,
    co2: String,
    co2raw: Int,
    co2LastUpdate: String
)

object EnvironmentOutput {
  implicit val encoder: Encoder[EnvironmentOutput] = deriveEncoder
}
