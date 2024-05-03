package rip.deadcode.sandbox_pi.db.writer

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.db.model.EnvSample

import java.time.{Instant, ZonedDateTime}
import java.util.UUID
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

@Singleton
class WriteStats @Inject() (jdbi: Jdbi) {

  private val logger = LoggerFactory.getLogger(classOf[WriteStats])

  /** @param table
    *   This will not be sanitized. Be careful for SQL injection.
    */
  def write(
      table: String,
      roomId: UUID,
      value: String,
      y: Int,
      mo: Int,
      d: Int,
      h: Int,
      mi: Int
  ): IO[Unit] = IO.blocking {

    val currentValue = jdbi.inTransaction { handle =>
      handle
        // language=SQL
        .createQuery(
          s"""select value, year, month, day, hour, minute
             |from $table
             |where room_id = :room_id and
             |      year = :year and
             |      month = :month and
             |      day = :day and
             |      hour = :hour and
             |      minute = :minute
             |""".stripMargin
        )
        .bind("room_id", roomId)
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
    } else {
      jdbi.inTransaction { handle =>
        handle
          // language=SQL
          .createUpdate(
            s"""insert into $table (value, year, month, day, hour, minute, room_id)
               |values (:value, :year, :month, :day, :hour, :minute, :room_id)
               |""".stripMargin
          )
          .bind("room_id", roomId)
          .bind("value", value)
          .bind("year", y)
          .bind("month", mo)
          .bind("day", d)
          .bind("hour", h)
          .bind("minute", mi)
          .execute()
      }
    }
  }.void
}
