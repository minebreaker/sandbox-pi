package rip.deadcode.sandbox_pi.service.mac

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import rip.deadcode.sandbox_pi.service.mac.ArpRunnerOutput.Item

final case class ArpRunnerOutput(
    items: Seq[Item]
)

object ArpRunnerOutput {

  implicit val encoder: Encoder[ArpRunnerOutput] = deriveEncoder
  implicit val itemEncoder: Encoder[Item] = deriveEncoder

  final case class Item(
      ip: String,
      mac: String
  )
}
