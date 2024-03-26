package rip.deadcode.sandbox_pi.lib.circe

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

implicit val zonedDateTimeEncoder: Encoder[ZonedDateTime] = (zdt: ZonedDateTime) => {
  Encoder.encodeString(zdt.format(DateTimeFormatter.ISO_DATE_TIME))
}

implicit val zonedDateTimeDecoder: Decoder[ZonedDateTime] = (c: HCursor) => {
  Decoder.decodeString.map(s => ZonedDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME))(c)
}
