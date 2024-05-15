package rip.deadcode.sandbox_pi.http.handler.stat

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import rip.deadcode.sandbox_pi.http.handler.stat.StatOutput.StatValue

case class StatOutput(
    temperature: Map[String, Option[StatValue]],
    pressure: Map[String, Option[StatValue]],
    humidity: Map[String, Option[StatValue]],
    co2: Map[String, Option[StatValue]],
    smell: Map[String, Option[StatValue]]
)

object StatOutput {
  implicit val encoder: Encoder[StatOutput] = deriveEncoder
  implicit val encoderHistoryValue: Encoder[StatValue] = deriveEncoder

  case class StatValue(
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
