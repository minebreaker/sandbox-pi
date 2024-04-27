package rip.deadcode.sandbox_pi.http.handler.arp

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.handler.arp.ArpOutput.Item
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import scala.collection.mutable.ListBuffer
import scala.sys.process.ProcessLogger
import scala.util.matching.compat.Regex

@Singleton
class ArpHandler @Inject() () extends HttpHandler {

  private val logger = LoggerFactory.getLogger(classOf[ArpHandler])

  override def url: Regex = "^/arp$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {
    IO.blocking {
      import sys.process.*
      val stdout = ListBuffer[String]()
      val stderr = ListBuffer[String]()
      val pl = ProcessLogger(stdout.append, stderr.append)
      val status = "arp -a" ! pl

      logger.debug("Arp result\n{}", stdout)

      if (status != 0) {
        logger.warn("Failed to execute arp process: {}\n{}", status, stderr.mkString("\n"))
        JsonHttpResponse.unknownError("Failed to execute arp process.")
      } else {
        val items = stdout.flatMap { line =>
          val ip = ipRegex.findFirstIn(line)
          val mac = macRegex.findFirstIn(line)
          (ip, mac) match {
            case (Some(ip), Some(mac)) => Some(Item(ip = ip, mac = mac))
            case _                     => None
          }
        }.toSeq
        JsonHttpResponse(
          200,
          ArpOutput(items)
        )
      }
    }
  }

  // https://stackoverflow.com/a/36760050
  private val ipRegex = "((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}".r

  //
  private val macRegex = "([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})".r
}
