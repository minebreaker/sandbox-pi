package rip.deadcode.sandbox_pi.http.handler.temperature

import com.squareup.moshi.Moshi
import rip.deadcode.sandbox_pi.json.JsonEncode

case class TemperatureOutput()

object TemperatureOutput {
  given (using moshi: Moshi): JsonEncode[TemperatureOutput] with {
    extension (self: TemperatureOutput) {
      override def encode(): String = moshi.adapter(classOf[TemperatureOutput]).toJson(self)
    }
  }
}
