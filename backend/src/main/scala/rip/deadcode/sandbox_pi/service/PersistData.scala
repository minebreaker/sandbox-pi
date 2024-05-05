package rip.deadcode.sandbox_pi.service

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.db.model.EnvSample
import rip.deadcode.sandbox_pi.db.writer.WriteStats
import rip.deadcode.sandbox_pi.lib.time.timestampToTime
import rip.deadcode.sandbox_pi.pi.bm680.Bme680Output
import rip.deadcode.sandbox_pi.pi.mhz19c.Mhz19cOutput
import rip.deadcode.sandbox_pi.service.PersistData.DefaultRoomId
import rip.deadcode.sandbox_pi.utils.{avg, med}

import java.time.{Clock, Instant, ZonedDateTime}
import java.util.UUID
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.math.Ordered.orderingToOrdered
import scala.reflect.ClassTag

@Singleton
private[service] class PersistData @Inject() (clock: Clock, writeStats: WriteStats) {

  private val logger = LoggerFactory.getLogger(classOf[PersistData])

  def persist(tph: Bme680Output, co2: Mhz19cOutput): IO[Unit] = {
    import cats.syntax.traverse.*
    for {
      (tphZdt, tphY, tphMo, tphD, tphH, tphMi) <- IO(timestampToTime(tph.timestamp, clock.getZone))
      (co2Zdt, co2Y, co2Mo, co2D, co2H, co2Mi) <- IO(timestampToTime(co2.timestamp, clock.getZone))
      _ <- Seq(
        ("temperature", tph.temp.toString),
        ("pressure", tph.press.toString),
        ("humidity", tph.hum.toString),
        ("co2", co2.co2.toString)
      ).map { (table, value) =>
        writeStats.write(table, DefaultRoomId, value, tphY, tphMo, tphD, tphH, tphMi)
      }.sequence
    } yield ()
  }
}

object PersistData {
  // We'll decouple sensors and the server, so this is a good-enough workaround fixed value.
  private val DefaultRoomId = UUID.fromString("57c91c78-99db-49fe-ae88-f9ef723aca9b")
}
