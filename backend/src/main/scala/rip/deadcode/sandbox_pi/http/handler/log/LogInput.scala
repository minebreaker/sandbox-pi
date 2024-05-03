package rip.deadcode.sandbox_pi.http.handler.log

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import rip.deadcode.sandbox_pi.http.handler.log.LogInput.LogItem

import java.time.ZonedDateTime

case class LogInput(
    items: Seq[LogItem]
)

object LogInput {

  import rip.deadcode.sandbox_pi.lib.circe.*
  implicit val decoder: Decoder[LogInput] = deriveDecoder
  implicit val itemDecoder: Decoder[LogItem] = deriveDecoder

  case class LogItem(
      target: String,
      roomId: String,
      value: String,
      timestamp: ZonedDateTime
  )
}
