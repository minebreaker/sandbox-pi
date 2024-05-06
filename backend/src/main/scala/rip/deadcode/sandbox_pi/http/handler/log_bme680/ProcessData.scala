package rip.deadcode.sandbox_pi.http.handler.log_bme680

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import rip.deadcode.sandbox_pi.db.writer.StatsWriter
import rip.deadcode.sandbox_pi.lib.time.timestampToTime
import rip.deadcode.sandbox_pi.pi.bm680.Bme680
import rip.deadcode.sandbox_pi.pi.mhz19c.Mhz19c

import java.nio.ByteBuffer
import java.time.Clock
import java.util.{Base64, UUID}

@Singleton
private[log_bme680] class ProcessData @Inject() (bme680: Bme680, clock: Clock, writeStats: StatsWriter) {

  import cats.syntax.traverse.*
  def run(input: LogBme680Input): IO[Unit] = {
    for {
      (_, y, mo, d, h, mi) <- IO(timestampToTime(clock.instant(), clock.getZone))
      roomId <- IO(UUID.fromString(input.roomId))

      // Why can't I use Tuple...? https://stackoverflow.com/q/4022436
      Seq(
        parT1,
        parT2T3,
        parP,
        parH,
        tempAdc,
        pressAdc,
        humAdc
      ) <- Seq(
        // (data, requiredLength)
        (input.parT1, 2),
        (input.parT2T3, 3),
        (input.parP, 19),
        (input.parH, 8),
        (input.tempAdc, 3),
        (input.pressAdc, 3),
        (input.humAdc, 2)
      ).traverse { case (p, len) =>
        for {
          decoded <- IO(Base64.getDecoder.decode(p))
          _ <- IO.raiseWhen(decoded.length != len)(???)
        } yield ByteBuffer.wrap(decoded)
      }

      calibrationData <- IO(bme680.processCalibrationData(parT1, parT2T3, parP, parH))
      result <- IO(bme680.processData(calibrationData, tempAdc, pressAdc, humAdc))

      _ <- Seq(
        ("temperature", result.temp),
        ("pressure", result.press),
        ("humidity", result.hum)
      ).traverse { case (table, value) =>
        writeStats.write(table, roomId, value.toString, y, mo, d, h, mi)
      }

    } yield ()
  }
}
