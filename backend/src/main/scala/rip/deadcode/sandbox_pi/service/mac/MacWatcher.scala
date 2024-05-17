package rip.deadcode.sandbox_pi.service.mac

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import rip.deadcode.sandbox_pi.db.reader.WatchMacReader

@Singleton
class MacWatcher @Inject() (arpRunner: ArpRunner, watchMacReader: WatchMacReader) {

  @volatile
  private var result: Map[String, Boolean] = Map.empty

  def isAtHome: Boolean = {
    // Consider the user is at home if all devices are at home.
    result.nonEmpty && result.values.forall(identity)
  }

  def run(): IO[Unit] = {
    for {
      currentResult <- arpRunner.run()
      watchingMacs <- watchMacReader.list()

      result = watchingMacs
        .map(_.mac.toLowerCase)
        .groupMap(m => m)(watchingMac => currentResult.items.exists(i => i.mac == watchingMac))
        .view
        .mapValues(_.head)
        .toMap

    } yield {
      this.result = result
    }
  }
}
