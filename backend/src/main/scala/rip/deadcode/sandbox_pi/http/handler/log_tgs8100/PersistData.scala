package rip.deadcode.sandbox_pi.http.handler.log_tgs8100

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import rip.deadcode.sandbox_pi.db.writer.StatsWriter
import rip.deadcode.sandbox_pi.lib.time.timestampToTime

import java.time.Clock
import java.util.UUID

@Singleton
private[log_tgs8100] class PersistData @Inject() (clock: Clock, statsWriter: StatsWriter) {

  def run(input: LogTgs8100Input): IO[Unit] = {
    for {
      (_, y, mo, d, h, mi) <- IO(timestampToTime(clock.instant(), clock.getZone))
      roomId <- IO(UUID.fromString(input.roomId))
      // make sure the value is a number. FIXME: validation
      _ <- IO(BigDecimal(input.value))

      _ <- statsWriter.write("smell", roomId, input.value, y, mo, d, h, mi)
    } yield ()
  }
}
