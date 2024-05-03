package rip.deadcode.sandbox_pi.http.handler.logger

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.jdbi.v3.core.Jdbi

import java.time.Clock

@Singleton
private[logger] class PersistData @Inject() (clock: Clock, jdbi: Jdbi) {

  def write(input: LoggerInput): IO[Unit] = {
    for {
      now <- IO(clock.instant())
      _ <- IO.blocking {

        jdbi.inTransaction { handle =>
          handle
            // language=SQL
            .createUpdate(
              """insert into log (device_id, level, value, timestamp) values(:device_id, :level, :value, :timestamp);
                |""".stripMargin
            )
            .bind("device_id", input.deviceId)
            .bind("level", input.level)
            .bind("value", input.value)
            .bind("timestamp", now)
            .execute()
        }
      }
    } yield ()
  }
}
