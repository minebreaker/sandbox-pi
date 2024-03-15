package rip.deadcode.sandbox_pi.http.handler.environment

import com.squareup.moshi.Moshi
import rip.deadcode.sandbox_pi.json.JsonEncode

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
  given (using moshi: Moshi): JsonEncode[EnvironmentOutput] with {
    extension (self: EnvironmentOutput) {
      override def encode(): String = moshi.adapter(classOf[EnvironmentOutput]).toJson(self)
    }
  }
}
