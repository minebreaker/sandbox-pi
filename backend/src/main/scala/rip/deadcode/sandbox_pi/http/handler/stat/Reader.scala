package rip.deadcode.sandbox_pi.http.handler.stat

import com.google.inject.{Inject, Singleton}
import org.jdbi.v3.core.Jdbi
import rip.deadcode.sandbox_pi.db.model.EnvSample
import rip.deadcode.sandbox_pi.http.handler.stat.Reader.{Summary, toOutput}
import rip.deadcode.sandbox_pi.http.handler.stat.StatOutput.StatValue
import rip.deadcode.sandbox_pi.utils.{avg, formatCo2, formatHumidity, formatPressure, formatTemperature, med}

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate, LocalDateTime, ZonedDateTime}
import java.util.UUID
import scala.jdk.CollectionConverters.*

@Singleton
private[stat] class Reader @Inject() (jdbi: Jdbi, clock: Clock) {

  def readHour(roomId: UUID): StatOutput = {
    val now = ZonedDateTime.now(clock)
    val y = now.getYear
    val mo = now.getMonthValue
    val d = now.getDayOfMonth
    val h = now.getHour

    val values = Tables
      .map { table =>
        jdbi
          .inTransaction { handle =>
            handle
              // language=SQL
              .createQuery(
                s"""select value, year, month, day, hour, minute
                   |from $table
                   |where room_id = :room_id and year = :year and month = :month and day = :day and hour = :hour
                   |""".stripMargin
              )
              .bind("room_id", roomId)
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
      .map { values =>
        values
          .groupBy(_.minute)
          .toSeq
          .map { case (i, values) =>
            val summary = Reader.Summary(values)
            (i.toString, summary)
          }
          .toMap
      }
    val init = Range(0, 59).map(i => (i.toString, None)).toMap[String, Option[Reader.Summary]]
    val temperature = init ++ values.head
    val pressure = init ++ values(1)
    val humidity = init ++ values(2)
    val co2 = init ++ values(3)
    val smell = init ++ values(4)

    toOutput(temperature, pressure, humidity, co2, smell)
  }

  def readDay(roomId: UUID): StatOutput = {
    val now = ZonedDateTime.now(clock)
    val y = now.getYear
    val mo = now.getMonthValue
    val d = now.getDayOfMonth

    val values = Tables
      .map { table =>
        jdbi
          .inTransaction { handle =>
            handle
              // language=SQL
              .createQuery(
                s"""select value, year, month, day, hour, minute
                   |from $table
                   |where room_id = :room_id and year = :year and month = :month and day = :day
                   |""".stripMargin
              )
              .bind("room_id", roomId)
              .bind("year", y)
              .bind("month", mo)
              .bind("day", d)
              .mapTo(classOf[EnvSample])
              .list()
              .asScala
              .toSeq
          }
      }
      .map { values =>
        values
          .groupBy(_.hour)
          .toSeq
          .map { case (i, values) =>
            val summary = Reader.Summary(values)
            (i.toString, summary)
          }
          .toMap
      }
    val init = Range(0, 24).map(i => (i.toString, None)).toMap[String, Option[Reader.Summary]]
    val temperature = init ++ values.head
    val pressure = init ++ values(1)
    val humidity = init ++ values(2)
    val co2 = init ++ values(3)
    val smell = init ++ values(4)

    toOutput(temperature, pressure, humidity, co2, smell)
  }

  def read7Days(roomId: UUID): StatOutput = {
    val now = ZonedDateTime.now(clock)
    val tY = now.getYear
    val tMo = now.getMonthValue
    val tD = now.getDayOfMonth
    val from = now.minusDays(6)
    val fY = from.getYear
    val fMo = from.getMonthValue
    val fD = from.getDayOfMonth

    val values = Tables
      .map { table =>
        jdbi
          .inTransaction { handle =>
            handle
              // language=SQL
              .createQuery(
                s"""select value, year, month, day, hour, minute
                   |from $table
                   |where
                   |  room_id = :room_id and
                   |  case
                   |    when :t_month = :f_month then
                   |      year = :t_year and month = :t_month and day <= :t_day and day >= :f_day
                   |    when :t_year = :f_year then
                   |      (year = :t_year and month = :t_month and day <= :t_day) or
                   |      (year = :t_year and month = :f_month and day >= :f_day)
                   |    else
                   |      (year = :t_year and month = :t_month and day <= :t_day) or
                   |      (year = :f_year and month = :f_month and day >= :f_day)
                   |  end
                   |""".stripMargin
              )
              .bind("room_id", roomId)
              .bind("t_year", tY)
              .bind("t_month", tMo)
              .bind("t_day", tD)
              .bind("f_year", fY)
              .bind("f_month", fMo)
              .bind("f_day", fD)
              .mapTo(classOf[EnvSample])
              .list()
              .asScala
              .toSeq
          }
      }
      .map { values =>
        values
          // This may create too many instances on the heap and can exhaust memories - need diagnosis
          .groupBy(s => LocalDate.of(s.year, s.month, s.day).format(DateTimeFormatter.ISO_DATE))
          .toSeq
          .map { case (day, values) =>
            val summary = Reader.Summary(values)
            (day, summary)
          }
          .toMap
      }
    val temperature = values.head
    val pressure = values(1)
    val humidity = values(2)
    val co2 = values(3)
    val smell = values(4)

    toOutput(temperature, pressure, humidity, co2, smell)
  }

  def readMonth(roomId: UUID): StatOutput = {
    val now = ZonedDateTime.now(clock)
    val y = now.getYear
    val mo = now.getMonthValue

    val values = Tables
      .map { table =>
        jdbi
          .inTransaction { handle =>
            handle
              // language=SQL
              .createQuery(
                s"""select value, year, month, day, hour, minute
                   |from $table
                   |where room_id = :room_id and year = :year and month = :month
                   |""".stripMargin
              )
              .bind("room_id", roomId)
              .bind("year", y)
              .bind("month", mo)
              .mapTo(classOf[EnvSample])
              .list()
              .asScala
              .toSeq
          }
      }
      .map { values =>
        values
          .groupBy(s => LocalDate.of(s.year, s.month, s.day).format(DateTimeFormatter.ISO_DATE))
          .toSeq
          .map { case (day, values) =>
            val summary = Reader.Summary(values)
            (day, summary)
          }
          .toMap
      }
    val temperature = values.head
    val pressure = values(1)
    val humidity = values(2)
    val co2 = values(3)
    val smell = values(4)

    toOutput(temperature, pressure, humidity, co2, smell)
  }

  private val Tables = Seq(
    "temperature",
    "pressure",
    "humidity",
    "co2",
    "smell"
  )
}

object Reader {

  private case class Summary(
      average: Double,
      median: Double,
      max: Double,
      min: Double
  ) {
    def toResult(format: Double => String): StatValue = StatValue(
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
      temperature: Map[String, Option[Summary]],
      pressure: Map[String, Option[Summary]],
      humidity: Map[String, Option[Summary]],
      co2: Map[String, Option[Summary]],
      smell: Map[String, Option[Summary]]
  ) = StatOutput(
    temperature = temperature.view.mapValues(_.map(_.toResult(formatTemperature))).toMap,
    pressure = pressure.view.mapValues(_.map(_.toResult(formatPressure))).toMap,
    humidity = humidity.view.mapValues(_.map(_.toResult(formatHumidity))).toMap,
    co2 = co2.view.mapValues(_.map(_.toResult(formatCo2))).toMap,
    smell = smell.view.mapValues(_.map(_.toResult(formatCo2))).toMap
  )
}
