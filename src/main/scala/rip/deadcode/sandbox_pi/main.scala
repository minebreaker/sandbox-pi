package rip.deadcode.sandbox_pi

import cats.effect.unsafe.IORuntime
import ch.qos.logback.classic.encoder.JsonEncoder
import com.google.common.net.MediaType
import com.google.inject.{Guice, Injector, Stage}
import com.pi4j.Pi4J
import io.circe.Encoder
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.{Request, Server, ServerConnector}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.db.{createDataSource, createJdbi, setupFlyway}
import rip.deadcode.sandbox_pi.http.HttpResponse.{JsonHttpResponse, StringHttpResponse}
import rip.deadcode.sandbox_pi.http.handler.environment.EnvironmentHandler
import rip.deadcode.sandbox_pi.http.handler.helloworld.HelloWorldHandler
import rip.deadcode.sandbox_pi.http.handler.led.LedHandler
import rip.deadcode.sandbox_pi.http.handler.pi_temperature.PiTemperatureHandler
import rip.deadcode.sandbox_pi.http.{Handlers, HttpHandler, HttpResponse, NotFoundHandler}
import rip.deadcode.sandbox_pi.service.Service

import java.time.{ZoneId, ZoneOffset}
import javax.sql.DataSource
import scala.util.chaining.scalaUtilChainingOps

@main
def main(): Unit = runServer()

private val logger = LoggerFactory.getLogger("rip.deadcode.sandbox_pi")

def runServer(): Unit = {

  logger.info("sandbox-pi starting up...")

  // pigpio requires root
  val user = System.getProperty("user.name")
  if (user != "root") {
    logger.info(s"May not run as root. Check the command. [user=$user]")
  }

  val config = readConfig()
  logger.info("Config: {}", config)

  val pi4j = Pi4J.newAutoContext()
  // Pi4J object seems like automatically shut down, so we don't need to manually call shutdown()

  val dataSource = createDataSource(config.database)
  setupFlyway(dataSource, config.database)
  val jdbi = createJdbi(dataSource)

  val stage = if (config.injector == "development") {
    Stage.DEVELOPMENT
  } else {
    Stage.PRODUCTION
  }

  val injector = Guice.createInjector(
    stage,
    new PiModule(
      config,
      pi4j,
      jdbi
    )
  )

  // Start a daemon thread
  val service = injector.getInstance(classOf[Service])
  service.start()

  val threadPool = QueuedThreadPool()
    .tap(_.setName("server"))
  val server = Server(threadPool)
  val connector = ServerConnector(server)
    .tap(_.setPort(config.port))
  server.addConnector(connector)

  val handlers = injector.getInstance(classOf[Handlers]).handlers
  val notFoundHandler = NotFoundHandler()

  server.setHandler(new AbstractHandler {
    override def handle(
        target: String,
        baseRequest: Request,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Unit = {

      implicit val catsRuntime: IORuntime = IORuntime.global

      logger.debug(s"Target url: $target")
      val targetHandler: HttpHandler = handlers
        .find(h => baseRequest.getMethod == h.method && h.url.matches(target))
        .getOrElse(notFoundHandler)
      val result =
        try {
          targetHandler.handle(baseRequest).handleError(handlerUnexpected).unsafeRunSync()
        } catch {
          case e: Exception =>
            logger.info("Error outside the IO")
            handlerUnexpected(e)
        }

      response.setStatus(result.status)
      result.header.foreach { case (name, value) =>
        response.setHeader(name, value)
      }
      result match {
        case StringHttpResponse(status, contentType, body, header) =>
          logger.debug("Response: string\n{}", body)
          response.setContentType(contentType.toString)
          response.getWriter.write(body)
        case e @ JsonHttpResponse(_, body, _) =>
          logger.debug(s"Response: JSON\n{}", body)
          response.setContentType(MediaType.JSON_UTF_8.toString)
          import io.circe.syntax.*
          response.getWriter.print(e.encode)
      }
      baseRequest.setHandled(true)
    }
  })
  server.start()
}

private def handlerUnexpected(e: Throwable) = {
  logger.warn("Unhandled exception", e)
  JsonHttpResponse.unknownError("Internal server error.")
}
