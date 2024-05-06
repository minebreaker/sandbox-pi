package rip.deadcode.sandbox_pi.http.handler.room

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import rip.deadcode.sandbox_pi.db.model.Room

case class ListRoomOutput(
    items: List[Room]
)

object ListRoomOutput {

  implicit val encoder: Encoder[ListRoomOutput] = deriveEncoder
  implicit val roomEncoder: Encoder[Room] = deriveEncoder
}
