package rip.deadcode.sandbox_pi.http.handler.helloworld

import com.squareup.moshi.Moshi
import rip.deadcode.sandbox_pi.json.JsonEncode

case class HelloWorldOutput(
    status: String
)

object HelloWorldOutput {
  given (using moshi: Moshi): JsonEncode[HelloWorldOutput] with {
    extension (self: HelloWorldOutput) {
      override def encode(): String = moshi.adapter(classOf[HelloWorldOutput]).toJson(self)
    }
  }
}
