package rip.deadcode.sandbox_pi.daemon

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.google.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.daemon.PeriodicRunner.RunnableSpec

import java.time.{Clock, Instant}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.FiniteDuration

@Singleton
class PeriodicRunner @Inject() (clock: Clock) {

  private val logger = LoggerFactory.getLogger(classOf[PeriodicRunner])

  import cats.syntax.traverse.*

  import scala.jdk.DurationConverters.*

  implicit val catsRuntime: IORuntime = IORuntime.global

  private var now = clock.instant()
  private var count = 0
  private val lastExecutionTimeMap = mutable.Map[Int, Instant]()
  private var runnables = ListBuffer[RunnableSpec]()

  def run(f: () => Unit): Unit = {
    this.synchronized {
      now = clock.instant()
      count = 0
      runnables = ListBuffer[RunnableSpec]()
      f()

      // Must be sequentially executed
      logger.debug(s"Task list: ${runnables.map(_.label).mkString(", ")}")
      runnables.toSeq
        .map { case RunnableSpec(label, io) =>
          if (logger.isDebugEnabled) {
            IO {
              logger.debug(s"Task: $label")
            } *> io
          } else {
            io
          }.recoverWith { e =>
            IO {
              logger.warn(s"Task failed. label: $label", e)
            }
          }
        }
        .sequence
        .unsafeRunSync()
    }
  }

  def every(period: FiniteDuration, label: String)(f: => IO[Unit]): Unit = {
    lastExecutionTimeMap.get(count) match {
      case Some(lastExec) if lastExec.isBefore(now.minus(period.toJava)) =>
        lastExecutionTimeMap.put(count, now)
        runnables += RunnableSpec(label, f)
      case None =>
        // First execution
        lastExecutionTimeMap.put(count, now)
        runnables += RunnableSpec(label, f)
        logger.debug(s"Adding task: ${label}, count: ${count}")
      case _ => ()
    }

    count = count + 1
  }
}

object PeriodicRunner {
  case class RunnableSpec(label: String, io: IO[Unit])
}
