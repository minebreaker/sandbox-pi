package rip.deadcode.sandbox_pi.service

import cats.effect.unsafe.IORuntime
import com.google.common.util.concurrent.MoreExecutors
import com.google.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.pi.bm680.Bme680
import rip.deadcode.sandbox_pi.pi.mhz19c.Mhz19c

import java.util.concurrent.{ScheduledExecutorService, ScheduledThreadPoolExecutor, TimeUnit}
import scala.util.{Failure, Success}

@Singleton
class Service @Inject() (
    bme680: Bme680,
    mhz19c: Mhz19c,
    persistData: PersistData,
    discord: Discord
) {

  import scala.concurrent.duration.*
  import scala.jdk.DurationConverters.*

  private val logger = LoggerFactory.getLogger(classOf[Service])

  private val PoolSize = 1
  private val Timeout = 10.seconds.toJava
  private val Period = 30.seconds.toJava

  private val executor: ScheduledExecutorService = MoreExecutors.getExitingScheduledExecutorService(
    new ScheduledThreadPoolExecutor(PoolSize),
    Period
  )

  def start(): Unit = {
    executor.scheduleAtFixedRate(new Runner, 0, Period.toMillis, TimeUnit.MILLISECONDS)
  }

  private class Runner extends Runnable {
    override def run(): Unit = {
      // FIXME
      implicit val catsRuntime: IORuntime = IORuntime.global
      try {
        logger.debug("Daemon started.")
        (for {
          tph <- bme680.refresh()
          co2 <- mhz19c.refresh()

          _ = persistData.persist(tph, co2)
          // FIXME
          _ = discord.run(tph, co2).unsafeRunSync()

        } yield ()) match {
          case Success(_) =>
            logger.debug("Daemon finished.")
          case Failure(e) =>
            logger.warn("Daemon failed to execute", e)
        }
      } catch {
        case e: Throwable => logger.warn("Unhandled exception at the daemon thread", e)
      }
    }
  }
}
