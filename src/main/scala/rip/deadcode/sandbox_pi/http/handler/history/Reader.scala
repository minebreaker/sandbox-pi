package rip.deadcode.sandbox_pi.http.handler.history

import com.google.inject.{Inject, Singleton}
import org.jdbi.v3.core.Jdbi
import rip.deadcode.sandbox_pi.db.model.EnvSample
import rip.deadcode.sandbox_pi.http.handler.history.HistoryOutput.HistoryValue
import rip.deadcode.sandbox_pi.http.handler.history.Reader.{Summary, toOutput}
import rip.deadcode.sandbox_pi.utils.{avg, formatCo2, formatHumidity, formatPressure, formatTemperature, med}

import scala.jdk.CollectionConverters.*

@Singleton
private[history] class Reader @Inject() (jdbi: Jdbi) {

  def readMonth(y: Int, mo: Int): HistoryOutput = {
    val values = Tables
      .map { table =>
        jdbi
          .inTransaction { handle =>
            handle
              // language=SQL
              .createQuery(
                s"""select value, year, month, day, hour, minute
                   |from $table
                   |where year = :year and month = :month
                   |""".stripMargin
              )
              .bind("year", y)
              .bind("month", mo)
              .mapTo(classOf[EnvSample])
              .list()
              .asScala
              .toSeq
          }
      }
      .map(Summary.apply)
    val temperature = values.head
    val pressure = values(1)
    val humidity = values(2)
    val co2 = values(3)

    toOutput(temperature, pressure, humidity, co2)
  }

  def readDay(y: Int, mo: Int, d: Int): HistoryOutput = {
    val values = Tables
      .map { table =>
        jdbi.inTransaction { handle =>
          handle
            // language=SQL
            .createQuery(
              s"""select value, year, month, day, hour, minute
               |from $table
               |where year = :year and month = :month and day = :day
               |""".stripMargin
            )
            .bind("year", y)
            .bind("month", mo)
            .bind("day", d)
            .mapTo(classOf[EnvSample])
            .list()
            .asScala
            .toSeq
        }
      }
      .map(Summary.apply)
    val temperature = values.head
    val pressure = values(1)
    val humidity = values(2)
    val co2 = values(3)

    toOutput(temperature, pressure, humidity, co2)
  }

  def readHour(y: Int, mo: Int, d: Int, h: Int): HistoryOutput = {
    val values = Tables
      .map { table =>
        jdbi.inTransaction { handle =>
          handle
            // language=SQL
            .createQuery(
              s"""select value, year, month, day, hour, minute
                 |from $table
                 |where year = :year and month = :month and day = :day and hour = :hour
                 |""".stripMargin
            )
            .bind("year", y)
            .bind("month", mo)
            .bind("day", d)
            .bind("hour", h)
            .mapTo(classOf[EnvSample])
            .list()
            .asScala
            .toSeq
        }
      }
      .map(Summary.apply)
    val temperature = values.head
    val pressure = values(1)
    val humidity = values(2)
    val co2 = values(3)

    toOutput(temperature, pressure, humidity, co2)
  }

  private val Tables = Seq(
    "temperature",
    "pressure",
    "humidity",
    "co2"
  )
}

object Reader {

  private case class Summary(
      average: Double,
      median: Double,
      max: Double,
      min: Double
  ) {
    def toResult(format: Double => String): HistoryValue = HistoryValue(
      format(this.average),
      this.average,
      format(this.median),
      this.median,
      format(this.max),
      this.max,
      format(this.min),
      this.min
    )
  }

  private object Summary {
    def apply(samples: Seq[EnvSample]): Option[Summary] = {
      if (samples.isEmpty) {
        None
      } else {
        val sample = samples.map(_.value.toDouble)
        Some(Summary(sample.avg, sample.med, sample.max, sample.min))
      }
    }
  }

  private def toOutput(
      temperature: Option[Summary],
      pressure: Option[Summary],
      humidity: Option[Summary],
      co2: Option[Summary]
  ) = HistoryOutput(
    temperature = temperature.map(_.toResult(formatTemperature)),
    pressure = pressure.map(_.toResult(formatPressure)),
    humidity = humidity.map(_.toResult(formatHumidity)),
    co2 = co2.map(_.toResult(formatCo2))
  )
}
