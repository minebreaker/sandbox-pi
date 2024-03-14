package rip.deadcode.sandbox_pi.http.handler.pi_temperature

import com.squareup.moshi.Moshi
import rip.deadcode.sandbox_pi.json.JsonEncode

case class PiTemperatureOutput(
    raw: Int,
    human: String
)

object PiTemperatureOutput {
  given (using moshi: Moshi): JsonEncode[PiTemperatureOutput] with {
    extension (self: PiTemperatureOutput) {
      override def encode(): String = moshi.adapter(classOf[PiTemperatureOutput]).toJson(self)
    }
  }
}
