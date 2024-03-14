package rip.deadcode.sandbox_pi.http

import cats.effect.IO
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.AppContext

import scala.util.matching.compat.Regex

trait HttpHandler:

  def url: Regex
  def method: String
  def handle(request: Request, ctx: AppContext): IO[HttpResponse]
