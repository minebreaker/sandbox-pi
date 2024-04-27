package rip.deadcode.sandbox_pi.http.handler.status

import cats.effect.IO
import com.google.common.net.MediaType
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.build_info.BuildInfo
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import scala.util.matching.compat.Regex

@Singleton
class StatusHandler @Inject() () extends HttpHandler {

  override def url: Regex = "^/status$".r

  override def method: String = "GET"

  private val output = StatusOutput(
    version = BuildInfo.version,
    scala = BuildInfo.scalaVersion,
    sbt = BuildInfo.sbtVersion
  )

  override def handle(request: Request): IO[HttpResponse] = {
    IO {
      JsonHttpResponse(
        200,
        output
      )
    }
  }
}
