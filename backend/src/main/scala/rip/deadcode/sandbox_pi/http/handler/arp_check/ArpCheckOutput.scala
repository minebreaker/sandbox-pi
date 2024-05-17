package rip.deadcode.sandbox_pi.http.handler.arp_check

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class ArpCheckOutput(
    atHome: Boolean
)

object ArpCheckOutput {
  implicit val encoder: Encoder[ArpCheckOutput] = deriveEncoder
}
