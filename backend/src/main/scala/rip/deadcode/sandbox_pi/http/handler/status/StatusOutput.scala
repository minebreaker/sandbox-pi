package rip.deadcode.sandbox_pi.http.handler.status

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class StatusOutput(
    version: String,
    scala: String,
    sbt: String
)

object StatusOutput {
  implicit val encoder: Encoder[StatusOutput] = deriveEncoder
}
