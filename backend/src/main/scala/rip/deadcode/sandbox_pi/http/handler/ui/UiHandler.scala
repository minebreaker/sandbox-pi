package rip.deadcode.sandbox_pi.http.handler.ui

import cats.effect.{IO, Resource}
import com.google.common.io.Resources
import com.google.common.net.MediaType
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.StringHttpResponse
import rip.deadcode.sandbox_pi.http.handler.ui.UiHandler.{html, js, urlMap}
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import java.nio.charset.StandardCharsets
import scala.util.matching.compat.Regex

@Singleton
class UiHandler @Inject() () extends HttpHandler {

  override def url: Regex = "^/ui(/.*)?$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {
    IO {
      val body = urlMap.getOrElse(request.getRequestURI, html)
      StringHttpResponse(
        200,
        MediaType.HTML_UTF_8,
        body
      )
    }
  }
}

object UiHandler {

  private val html = Resources.toString(Resources.getResource("ui/index.html"), StandardCharsets.UTF_8)
  private val js = Resources.toString(Resources.getResource("ui/main.js"), StandardCharsets.UTF_8)
  private val jsMap = Resources.toString(Resources.getResource("ui/main.js.map"), StandardCharsets.UTF_8)

  private val urlMap = Map(
    "/ui/index.js" -> js,
    "/ui/index.js.map" -> jsMap
  )
}
