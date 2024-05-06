package rip.deadcode.sandbox_pi.http.handler.log

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import rip.deadcode.sandbox_pi.db.writer.StatsWriter
import rip.deadcode.sandbox_pi.http.handler.log.LogException.InvalidParameter

import java.util.UUID
import scala.concurrent.Future

@Singleton
private[log] class PersistData @Inject() (writeStats: StatsWriter) {

  def persist(input: LogInput): IO[Unit] = {
    import cats.syntax.traverse.*
    input.items.traverse { i =>
      writeStats.write(
        i.target,
        UUID.fromString(i.roomId), // TODO validation
        i.value,
        i.timestamp.getYear,
        i.timestamp.getMonthValue,
        i.timestamp.getDayOfMonth,
        i.timestamp.getHour,
        i.timestamp.getMinute
      )
    }.void
  }
}
