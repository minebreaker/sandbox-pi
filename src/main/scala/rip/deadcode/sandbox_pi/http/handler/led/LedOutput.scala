package rip.deadcode.sandbox_pi.http.handler.led

import com.squareup.moshi.Moshi
import rip.deadcode.sandbox_pi.json.JsonEncode

case class LedOutput(
    status: String
)

object LedOutput {
  given (using moshi: Moshi): JsonEncode[LedOutput] with {
    extension (self: LedOutput) {
      override def encode(): String = moshi.adapter(classOf[LedOutput]).toJson(self)
    }
  }
}
