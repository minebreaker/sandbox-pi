package rip.deadcode.sandbox_pi.http.handler.room

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.db.reader.RoomReader
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import scala.util.matching.compat.Regex

@Singleton
class ListRoomHandler @Inject() (roomReader: RoomReader) extends HttpHandler {

  override def url: Regex = "^/room$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {

    for {
      rooms <- roomReader.list()
    } yield JsonHttpResponse(
      200,
      ListRoomOutput(
        rooms
      )
    )
  }
}
