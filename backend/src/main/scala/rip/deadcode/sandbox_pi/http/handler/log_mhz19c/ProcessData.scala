package rip.deadcode.sandbox_pi.http.handler.log_mhz19c

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import rip.deadcode.sandbox_pi.db.writer.WriteStats
import rip.deadcode.sandbox_pi.lib.time.timestampToTime
import rip.deadcode.sandbox_pi.pi.mhz19c.Mhz19c

import java.nio.ByteBuffer
import java.time.Clock
import java.util.{Base64, UUID}

@Singleton
class ProcessData @Inject() (mhz19c: Mhz19c, clock: Clock, writeStats: WriteStats) {

  def run(input: LogMhz19CInput): IO[Unit] = {
    for {
      (_, y, mo, d, h, mi) <- IO(timestampToTime(clock.instant(), clock.getZone))
      roomId <- IO(UUID.fromString(input.roomId))
      // Non-url
      decoded <- IO(Base64.getDecoder.decode(input.value))
      _ <- IO.raiseWhen(decoded.length != 9)(???)
      result <- IO(mhz19c.processData(ByteBuffer.wrap(decoded)))

      // Let FK make inserting counterfeit records fail... should be fixed someday
      _ <- writeStats.write("co2", roomId, result.toString, y, mo, d, h, mi)

    } yield ()
  }
}
