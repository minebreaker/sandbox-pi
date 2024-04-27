package rip.deadcode.sandbox_pi.http.handler.history

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import rip.deadcode.sandbox_pi.http.handler.history.HistoryOutput.HistoryValue

case class HistoryOutput(
    temperature: Option[HistoryValue],
    pressure: Option[HistoryValue],
    humidity: Option[HistoryValue],
    co2: Option[HistoryValue]
)

object HistoryOutput {
  implicit val encoder: Encoder[HistoryOutput] = deriveEncoder
  implicit val encoderHistoryValue: Encoder[HistoryValue] = deriveEncoder

  case class HistoryValue(
      average: String,
      averageRaw: Double,
      median: String,
      medianRaw: Double,
      max: String,
      maxRaw: Double,
      min: String,
      minRaw: Double
  )
}
