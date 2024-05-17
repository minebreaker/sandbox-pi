package rip.deadcode.sandbox_pi.db.reader

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.jdbi.v3.core.Jdbi
import rip.deadcode.sandbox_pi.db.model.WatchMac

import scala.jdk.CollectionConverters.*

@Singleton
class WatchMacReader @Inject() (jdbi: Jdbi) {

  def list(): IO[List[WatchMac]] = IO.blocking {

    jdbi.inTransaction { handle =>
      handle
        // language=SQL
        .createQuery(
          s"""select mac
             |from watch_mac
             |""".stripMargin
        )
        .mapTo(classOf[WatchMac])
        .list()
        .asScala
        .toList
    }
  }
}
