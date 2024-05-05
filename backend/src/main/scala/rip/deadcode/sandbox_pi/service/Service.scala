package rip.deadcode.sandbox_pi.service

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.google.common.util.concurrent.MoreExecutors
import com.google.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.pi.bm680.Bme680
import rip.deadcode.sandbox_pi.pi.mhz19c.Mhz19c

import java.time.{Clock, Instant}
import java.util.concurrent.{ScheduledExecutorService, ScheduledThreadPoolExecutor, TimeUnit}
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

@Singleton
class Service @Inject() (
    bme680: Bme680,
    mhz19c: Mhz19c,
    persistData: PersistData,
    discord: Discord,
    runner: PeriodicRunner
) {

  import scala.concurrent.duration.*
  import scala.jdk.DurationConverters.*

  private val logger = LoggerFactory.getLogger(classOf[Service])

  private val PoolSize = 1
  private val Timeout = 10.seconds.toJava
  private val Period = 10.seconds.toJava

  private val executor: ScheduledExecutorService = MoreExecutors.getExitingScheduledExecutorService(
    new ScheduledThreadPoolExecutor(PoolSize),
    Period
  )

  def start(): Unit = {
    executor.scheduleAtFixedRate(new Runner, 0, Period.toMillis, TimeUnit.MILLISECONDS)
  }

  private class Runner extends Runnable {
    override def run(): Unit = {
      try {
        logger.debug("Daemon started.")

        runner.run { () =>
          import runner.*

          import scala.concurrent.duration.*
          every(30.seconds, "Update env info") {
            for {
              (tph, co2) <- IO.blocking {
                (for {
                  // FIXME
                  tph <- bme680.refresh()
                  co2 <- mhz19c.refresh()
                } yield (tph, co2)).get
              }
              _ <- persistData.persist(tph, co2)
            } yield ()
          }
          every(30.minutes, "Discord notification") {
            discord.run()
          }
        }

      } catch {
        case e: Throwable => logger.warn("Unhandled exception at the daemon thread", e)
      }
    }
  }
}
