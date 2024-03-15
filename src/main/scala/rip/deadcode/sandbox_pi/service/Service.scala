package rip.deadcode.sandbox_pi.service

import com.google.common.util.concurrent.MoreExecutors
import com.google.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.pi.bm680.Bme680
import rip.deadcode.sandbox_pi.pi.mhz19c.Mhz19c

import java.util.concurrent.{ScheduledExecutorService, ScheduledThreadPoolExecutor, TimeUnit}

@Singleton
class Service @Inject() (
    bme680: Bme680,
    mhz19c: Mhz19c
) {

  import scala.concurrent.duration.*
  import scala.jdk.DurationConverters.*

  private val logger = LoggerFactory.getLogger(classOf[Service])

  private val PoolSize = 1
  private val Timeout = 10.seconds.toJava
  private val Period = 60.seconds.toJava

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
        for {
          _ <- bme680.refresh()
          _ <- mhz19c.refresh()
        } yield ()
      } catch {
        case e: Throwable => logger.warn("Unhandled exception at the daemon thread", e)
      }
    }
  }
}
