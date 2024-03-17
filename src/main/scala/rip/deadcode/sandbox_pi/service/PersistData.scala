package rip.deadcode.sandbox_pi.service

import com.google.inject.{Inject, Singleton}
import org.jdbi.v3.core.Jdbi
import rip.deadcode.sandbox_pi.db.model.{DayValue, HourValue, MinuteValue}
import rip.deadcode.sandbox_pi.pi.bm680.Bme680Output
import rip.deadcode.sandbox_pi.pi.mhz19c.Mhz19cOutput
import rip.deadcode.sandbox_pi.utils.{avg, med}

import java.time.{Clock, Instant, ZonedDateTime}
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.math.Ordered.orderingToOrdered
import scala.reflect.ClassTag

@Singleton
class PersistData @Inject() (jdbi: Jdbi, clock: Clock) {

  def persist(tph: Bme680Output, co2: Mhz19cOutput): Unit = {

    val (tphZdt, tphY, tphMo, tphD, tphH, tphMi) = timestampToTime(tph.timestamp)
    val (co2Zdt, co2Y, co2Mo, co2D, co2H, co2Mi) = timestampToTime(co2.timestamp)

    Seq(
      "temperature",
      "pressure",
      "humidity",
      "co2"
    ).map(v => (s"${v}_minute", s"${v}_hour", s"${v}_day", s"${v}_month"))
      .foreach { (tblMi, tblH, tblD, tblMo) =>
        persistPerMinutes(tblMi, tph.temp.toString, tphY, tphMo, tphD, tphH, tphMi)
        if (tphMi < 5) {
          persistPerHour(tblH, tblMi, tphZdt)
          persistPerDay(tblH, tblMi, tphZdt)
          persistPerMonth(tblH, tblMi, tphZdt)
        }
      }
  }

  private def timestampToTime(ts: Instant) = {
    val dt = ZonedDateTime.ofInstant(ts, clock.getZone)
    ztimeToTime(dt)
  }

  private def ztimeToTime(dt: ZonedDateTime) = {
    (dt, dt.getYear, dt.getMonthValue, dt.getDayOfMonth, dt.getHour, dt.getMinute)
  }

  /** @param table
    *   Be careful for SQL injection
    */
  private def persistPerMinutes(
      table: String,
      value: String,
      y: Int,
      mo: Int,
      d: Int,
      h: Int,
      mi: Int
  ): Unit = {

    val currentValue = jdbi.inTransaction { handle =>
      handle
        // language=SQL
        .createQuery(
          s"""select year, month, day, hour, minute
             |from $table
             |where year = :year and month = :month and day = :day and minute = :minute
             |""".stripMargin
        )
        .bind("year", y)
        .bind("month", mo)
        .bind("day", d)
        .bind("hour", h)
        .bind("minute", mi)
        .mapTo(classOf[MinuteValue])
        .findOne()
        .toScala
    }

    if (currentValue.isDefined) {
      return
    }

    jdbi.inTransaction { handle =>
      handle
        // language=SQL
        .createUpdate(
          """insert into $table (value, year, month, day, hour, minute)
              |values (:value, :year, :month, :day, :hour, :minute)
              |""".stripMargin
        )
        .bind("value", value)
        .bind("year", y)
        .bind("month", mo)
        .bind("day", d)
        .bind("hour", h)
        .bind("minute", mi)
        .execute()
    }
  }

  private def persistPerHour(
      table: String,
      minuteTable: String,
      time: ZonedDateTime
  ): Unit = {

    val (_, wY, wMo, wD, wH, wMi) = ztimeToTime(time.minusHours(1))

    val currentValue = jdbi.inTransaction { handle =>
      handle
        // language=SQL
        .createQuery(
          s"""select year, month, day, hour
             |from $table
             |where year = :year and month = :month and day = :day and hour = :hour
             |""".stripMargin
        )
        .bind("year", wY)
        .bind("month", wMo)
        .bind("day", wD)
        .bind("hour", wH)
        .mapTo(classOf[HourValue])
        .findOne()
        .toScala
    }

    if (currentValue.isDefined) {
      return
    }

    val window = jdbi
      .inTransaction { handle =>
        handle
          // language=SQL
          .createQuery(
            s"""select year, month, day, hour
             |from $minuteTable
             |where year = :year and month = :month and day = :day
             |""".stripMargin
          )
          .bind("year", wY)
          .bind("month", wMo)
          .bind("day", wD)
          .bind("hour", wH)
          .mapTo(classOf[MinuteValue])
          .list()
          .asScala
          .toSeq
      }
      .map(_.value.toDouble)

    jdbi.inTransaction { handle =>
      handle
        // language=SQL
        .createUpdate(
          """insert into $table (average, median, max, min, year, month, day, hour)
            |values (:average, :median, :max, :min, :year, :month, :day, :hour)
            |""".stripMargin
        )
        .bind("average", window.avg)
        .bind("median", window.med)
        .bind("max", window.max)
        .bind("min", window.min)
        .bind("year", wY)
        .bind("month", wMo)
        .bind("day", wD)
        .bind("hour", wH)
        .execute()
    }
  }

  private def persistPerDay(
      table: String,
      minuteTable: String,
      time: ZonedDateTime
  ): Unit = {

    val (_, wY, wMo, wD, wH, wMi) = ztimeToTime(time.minusDays(1))

    val currentValue = jdbi.inTransaction { handle =>
      handle
        // language=SQL
        .createQuery(
          s"""select year, month, day
             |from $table
             |where year = :year and month = :month and day = :day
             |""".stripMargin
        )
        .bind("year", wY)
        .bind("month", wMo)
        .bind("day", wD)
        .mapTo(classOf[DayValue])
        .findOne()
        .toScala
    }

    if (currentValue.isDefined) {
      return
    }

    val window = jdbi
      .inTransaction { handle =>
        handle
          // language=SQL
          .createQuery(
            s"""select year, month, day, hour
             |from $minuteTable
             |where year = :year and month = :month and day = :day
             |""".stripMargin
          )
          .bind("year", wY)
          .bind("month", wMo)
          .bind("day", wD)
          .mapTo(classOf[MinuteValue])
          .list()
          .asScala
          .toSeq
      }
      .map(_.value.toDouble)

    jdbi.inTransaction { handle =>
      handle
        // language=SQL
        .createUpdate(
          """insert into $table (average, median, max, min, year, month, day)
            |values (:average, :median, :max, :min, :year, :month, :day)
            |""".stripMargin
        )
        .bind("average", window.avg)
        .bind("median", window.med)
        .bind("max", window.max)
        .bind("min", window.min)
        .bind("year", wY)
        .bind("month", wMo)
        .bind("day", wD)
        .execute()
    }
  }

  private def persistPerMonth(
      table: String,
      minuteTable: String,
      time: ZonedDateTime
  ): Unit = {

    val (_, wY, wMo, wD, _, _) = ztimeToTime(time.minusMonths(1))

    val currentValue = jdbi.inTransaction { handle =>
      handle
        // language=SQL
        .createQuery(
          s"""select year, month
             |from $table
             |where year = :year and month = :month
             |""".stripMargin
        )
        .bind("year", wY)
        .bind("month", wMo)
        .mapTo(classOf[HourValue])
        .findOne()
        .toScala
    }

    if (currentValue.isDefined) {
      return
    }

    val window = jdbi
      .inTransaction { handle =>
        handle
          // language=SQL
          .createQuery(
            s"""select year, month, day, hour
             |from $minuteTable
             |where year = :year and month = :month and day = :day
             |""".stripMargin
          )
          .bind("year", wY)
          .bind("month", wMo)
          .mapTo(classOf[MinuteValue])
          .list()
          .asScala
          .toSeq
      }
      .map(_.value.toDouble)

    jdbi.inTransaction { handle =>
      handle
        // language=SQL
        .createUpdate(
          """insert into $table (average, median, max, min, year, month)
            |values (:average, :median, :max, :min, :year, :month)
            |""".stripMargin
        )
        .bind("average", window.avg)
        .bind("median", window.med)
        .bind("max", window.max)
        .bind("min", window.min)
        .bind("year", wY)
        .bind("month", wMo)
        .execute()
    }
  }
}
