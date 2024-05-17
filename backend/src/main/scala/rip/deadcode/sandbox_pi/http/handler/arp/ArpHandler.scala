package rip.deadcode.sandbox_pi.http.handler.arp

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}
import rip.deadcode.sandbox_pi.service.mac.ArpRunner

import scala.collection.mutable.ListBuffer
import scala.sys.process.ProcessLogger
import scala.util.matching.compat.Regex

@Singleton
class ArpHandler @Inject() (arpRunner: ArpRunner) extends HttpHandler {

  private val logger = LoggerFactory.getLogger(classOf[ArpHandler])

  override def url: Regex = "^/arp$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {
    for {
      result <- arpRunner.run()
    } yield JsonHttpResponse(
      200,
      result
    )
  }
}
