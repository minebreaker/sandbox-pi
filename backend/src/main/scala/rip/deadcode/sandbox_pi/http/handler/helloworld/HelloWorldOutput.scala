package rip.deadcode.sandbox_pi.http.handler.helloworld

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class HelloWorldOutput(
    status: String
)

object HelloWorldOutput {
  implicit val encoder: Encoder[HelloWorldOutput] = deriveEncoder
}
