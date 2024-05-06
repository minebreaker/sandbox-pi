package rip.deadcode.sandbox_pi.db.reader

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.db.model.Room

import scala.jdk.CollectionConverters.*

@Singleton
class RoomReader @Inject() (jdbi: Jdbi) {

  private val logger = LoggerFactory.getLogger(classOf[RoomReader])

  def list(): IO[List[Room]] = IO.blocking {

    jdbi.inTransaction { handle =>
      handle
        // language=SQL
        .createQuery(
          s"""select id, name
           |from room
           |""".stripMargin
        )
        .mapTo(classOf[Room])
        .list()
        .asScala
        .toList
    }
  }
}
