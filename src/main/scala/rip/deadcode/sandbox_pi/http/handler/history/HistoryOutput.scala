package rip.deadcode.sandbox_pi.http.handler.history

import com.squareup.moshi.Moshi
import rip.deadcode.sandbox_pi.json.JsonEncode

case class HistoryOutput(
    temperature: Option[HistoryValue],
    pressure: Option[HistoryValue],
    humidity: Option[HistoryValue],
    co2: Option[HistoryValue]
)

object HistoryOutput {
  given (using moshi: Moshi): JsonEncode[HistoryOutput] with {
    extension (self: HistoryOutput) {
      override def encode(): String = moshi.adapter(classOf[HistoryOutput]).toJson(self)
    }
  }
}

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
