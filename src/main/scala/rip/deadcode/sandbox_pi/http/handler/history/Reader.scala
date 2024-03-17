package rip.deadcode.sandbox_pi.http.handler.history

import com.google.inject.{Inject, Singleton}
import org.jdbi.v3.core.Jdbi
import rip.deadcode.sandbox_pi.db.model.{DayValue, MonthValue, Values}
import rip.deadcode.sandbox_pi.utils.{formatCo2, formatHumidity, formatPressure, formatTemperature}
import scala.jdk.OptionConverters.*

@Singleton
private[history] class Reader @Inject() (jdbi: Jdbi) {

  def readMonth(y: Int, mo: Int): HistoryOutput = {
    val values = Tables.map(t => s"${t}_month").map { table =>
      jdbi.inTransaction { handle =>
        handle
          // language=SQL
          .createQuery(
            s"""select average, median, max_value, min_value, year, month
                 |from $table
                 |where year = :year and month = :month
                 |""".stripMargin
          )
          .bind("year", y)
          .bind("month", mo)
          .mapTo(classOf[MonthValue])
          .findOne()
          .toScala
      }
    }
    val temperature = values.head
    val pressure = values(1)
    val humidity = values(2)
    val co2 = values(3)

    toOutput(temperature, pressure, humidity, co2)
  }

  def readDay(y: Int, mo: Int, d: Int): HistoryOutput = {
    val values = Tables.map(t => s"${t}_day").map { table =>
      jdbi.inTransaction { handle =>
        handle
          // language=SQL
          .createQuery(
            s"""select average, median, max_value, min_value, year, month, day
               |from $table
               |where year = :year and month = :month and day = :day
               |""".stripMargin
          )
          .bind("year", y)
          .bind("month", mo)
          .bind("day", d)
          .mapTo(classOf[DayValue])
          .findOne()
          .toScala
      }
    }
    val temperature = values.head
    val pressure = values(1)
    val humidity = values(2)
    val co2 = values(3)

    toOutput(temperature, pressure, humidity, co2)
  }

  def readHour(y: Int, mo: Int, d: Int, h: Int): HistoryOutput = {
    val values = Tables.map(t => s"${t}_hour").map { table =>
      jdbi.inTransaction { handle =>
        handle
          // language=SQL
          .createQuery(
            s"""select average, median, max_value, min_value, year, month, day, hour
               |from $table
               |where year = :year and month = :month and day = :day and hour = :hour
               |""".stripMargin
          )
          .bind("year", y)
          .bind("month", mo)
          .bind("day", d)
          .bind("hour", h)
          .mapTo(classOf[DayValue])
          .findOne()
          .toScala
      }
    }
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

  private def toOutput(
      temperature: Option[Values],
      pressure: Option[Values],
      humidity: Option[Values],
      co2: Option[Values]
  ) = HistoryOutput(
    temperature = temperature.map(v =>
      HistoryValue(
        formatTemperature(v.average),
        v.average.toDouble,
        formatTemperature(v.median),
        v.median.toDouble,
        formatTemperature(v.max),
        v.max.toDouble,
        formatTemperature(v.min),
        v.min.toDouble
      )
    ),
    pressure = pressure.map(v =>
      HistoryValue(
        formatPressure(v.average),
        v.average.toDouble,
        formatPressure(v.median),
        v.median.toDouble,
        formatPressure(v.max),
        v.max.toDouble,
        formatPressure(v.min),
        v.min.toDouble
      )
    ),
    humidity = humidity.map(v =>
      HistoryValue(
        formatHumidity(v.average),
        v.average.toDouble,
        formatHumidity(v.median),
        v.median.toDouble,
        formatHumidity(v.max),
        v.max.toDouble,
        formatHumidity(v.min),
        v.min.toDouble
      )
    ),
    co2 = co2.map(v =>
      HistoryValue(
        formatCo2(v.average),
        v.average.toDouble,
        formatCo2(v.median),
        v.median.toDouble,
        formatCo2(v.max),
        v.max.toDouble,
        formatCo2(v.min),
        v.min.toDouble
      )
    )
  )
}
