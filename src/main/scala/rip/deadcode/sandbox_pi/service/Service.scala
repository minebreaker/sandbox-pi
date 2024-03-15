package rip.deadcode.sandbox_pi.service

import com.google.common.util.concurrent.MoreExecutors
import com.google.inject.{Inject, Singleton}

import java.util.concurrent.{ScheduledExecutorService, ScheduledThreadPoolExecutor, TimeUnit}

@Singleton
class Service @Inject() () {

  import scala.concurrent.duration.*
  import scala.jdk.DurationConverters.*

  private val PoolSize = 4
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
    override def run(): Unit = {}
  }
}
