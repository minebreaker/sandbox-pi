package rip.deadcode.sandbox_pi

import com.google.common.util.concurrent.MoreExecutors

import java.util.concurrent.{ScheduledExecutorService, ScheduledThreadPoolExecutor}

object Scheduler {

  import scala.concurrent.duration.*
  import scala.jdk.DurationConverters.*

  private val PoolSize = 4
  private val Timeout = 60.seconds.toJava

  val executor: ScheduledExecutorService = MoreExecutors.getExitingScheduledExecutorService(
    new ScheduledThreadPoolExecutor(PoolSize),
    Timeout
  )
}
