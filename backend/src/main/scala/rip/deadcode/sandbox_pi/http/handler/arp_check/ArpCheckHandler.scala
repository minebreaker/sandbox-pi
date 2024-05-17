package rip.deadcode.sandbox_pi.http.handler.arp_check

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}
import rip.deadcode.sandbox_pi.service.mac.{ArpRunner, MacWatcher}

import scala.util.matching.compat.Regex

@Singleton
class ArpCheckHandler @Inject() (macWatcher: MacWatcher) extends HttpHandler {

  override def url: Regex = "/arp/are-you-at-home".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {
    IO {
      val response = ArpCheckOutput(macWatcher.isAtHome)
      JsonHttpResponse(200, response)
    }
  }
}
