package rip.deadcode.sandbox_pi.http.handler.arp

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import rip.deadcode.sandbox_pi.http.handler.arp.ArpOutput.Item

final case class ArpOutput(
    items: Seq[Item]
)

object ArpOutput {

  implicit val encoder: Encoder[ArpOutput] = deriveEncoder
  implicit val itemEncoder: Encoder[Item] = deriveEncoder

  final case class Item(
      ip: String,
      mac: String
  )
}
