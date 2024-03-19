package rip.deadcode.sandbox_pi.service

import com.google.inject.{Inject, Singleton}
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.db.model.EnvSample
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

  private val logger = LoggerFactory.getLogger(classOf[PersistData])

  def persist(tph: Bme680Output, co2: Mhz19cOutput): Unit = {

    val (tphZdt, tphY, tphMo, tphD, tphH, tphMi) = timestampToTime(tph.timestamp)
    val (co2Zdt, co2Y, co2Mo, co2D, co2H, co2Mi) = timestampToTime(co2.timestamp)

    Seq(
      ("temperature", tph.temp.toString),
      ("pressure", tph.press.toString),
      ("humidity", tph.hum.toString),
      ("co2", co2.co2.toString)
    ).foreach { (table, value) =>
      persistPerMinutes(table, value, tphY, tphMo, tphD, tphH, tphMi)
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
          s"""select value, year, month, day, hour, minute
             |from $table
             |where year = :year and month = :month and day = :day and minute = :minute
             |""".stripMargin
        )
        .bind("year", y)
        .bind("month", mo)
        .bind("day", d)
        .bind("hour", h)
        .bind("minute", mi)
        .mapTo(classOf[EnvSample])
        .findOne()
        .toScala
    }

    if (currentValue.isDefined) {
      logger.debug("Already saved.")
      return
    }

    jdbi.inTransaction { handle =>
      handle
        // language=SQL
        .createUpdate(
          s"""insert into $table (value, year, month, day, hour, minute)
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
}
