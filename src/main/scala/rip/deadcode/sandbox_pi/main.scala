package rip.deadcode.sandbox_pi

import cats.effect.unsafe.IORuntime
import ch.qos.logback.classic.encoder.JsonEncoder
import com.google.common.net.MediaType
import com.google.inject.{Guice, Injector, Stage}
import com.pi4j.Pi4J
import com.squareup.moshi.Moshi
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.{Request, Server, ServerConnector}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.db.{createDataSource, createJdbi, setupFlyway}
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.handler.helloworld.HelloWorldHandler
import rip.deadcode.sandbox_pi.http.handler.led.LedHandler
import rip.deadcode.sandbox_pi.http.handler.pi_temperature.PiTemperatureHandler
import rip.deadcode.sandbox_pi.http.handler.environment.EnvironmentHandler
import rip.deadcode.sandbox_pi.http.{Handlers, HttpHandler, NotFoundHandler}
import rip.deadcode.sandbox_pi.json.{JsonEncode, ScalaAdapter}
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

  implicit val moshi: Moshi = Moshi
    .Builder()
    .add(ScalaAdapter())
    .build()

  val stage = if (config.injector == "development") {
    Stage.DEVELOPMENT
  } else {
    Stage.PRODUCTION
  }

  val guice = Guice.createInjector(
    stage,
    new PiModule(
      config,
      pi4j,
      moshi,
      jdbi
    )
  )

  // Start a daemon thread
  val service = guice.getInstance(classOf[Service])
  service.start()

  val threadPool = QueuedThreadPool()
    .tap(_.setName("server"))
  val server = Server(threadPool)
  val connector = ServerConnector(server)
    .tap(_.setPort(config.port))
  server.addConnector(connector)

  val handlers = guice.getInstance(classOf[Handlers]).handlers
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
        case e @ JsonHttpResponse(_, body, _) =>
          logger.debug(s"Response: JSON\n{}", body)
          response.setContentType(MediaType.JSON_UTF_8.toString)
          response.getWriter.print(e.encode())
      }
      baseRequest.setHandled(true)
    }
  })
  server.start()
}

case class ErrorResponse(message: String)
given (using moshi: Moshi): JsonEncode[ErrorResponse] with {
  extension (self: ErrorResponse) override def encode(): String = moshi.adapter(classOf[ErrorResponse]).toJson(self)
}

private def handlerUnexpected(e: Throwable)(using moshi: Moshi) = {

  logger.warn("Unhandled exception", e)
  JsonHttpResponse(
    status = 500,
    ErrorResponse("Internal server error.")
  )
}
