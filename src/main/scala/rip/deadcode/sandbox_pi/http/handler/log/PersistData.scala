package rip.deadcode.sandbox_pi.http.handler.log

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import rip.deadcode.sandbox_pi.db.writer.WriteStats
import rip.deadcode.sandbox_pi.http.handler.log.LogException.InvalidParameter

import scala.concurrent.Future

@Singleton
class PersistData @Inject() (writeStats: WriteStats) {

  def persist(input: LogInput): IO[Unit] = {
    import cats.syntax.traverse.*
    input.items.traverse { i =>
      IO.blocking {
        writeStats.write(
          i.target,
          i.value,
          i.timestamp.getYear,
          i.timestamp.getMonthValue,
          i.timestamp.getDayOfMonth,
          i.timestamp.getHour,
          i.timestamp.getMinute
        )
      }
    }.void
  }
}
