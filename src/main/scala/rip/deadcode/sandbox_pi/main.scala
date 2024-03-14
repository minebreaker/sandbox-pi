package rip.deadcode.sandbox_pi

import cats.effect.unsafe.IORuntime
import ch.qos.logback.classic.encoder.JsonEncoder
import com.google.common.net.MediaType
import com.pi4j.Pi4J
import com.squareup.moshi.Moshi
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.{Request, Server, ServerConnector}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.http.HttpResponse.JsonHttpResponse
import rip.deadcode.sandbox_pi.http.handler.helloworld.HelloWorldHandler
import rip.deadcode.sandbox_pi.http.handler.led.LedHandler
import rip.deadcode.sandbox_pi.http.handler.pi_temperature.PiTemperatureHandler
import rip.deadcode.sandbox_pi.http.handler.temperature.TemperatureHandler
import rip.deadcode.sandbox_pi.http.{HttpHandler, NotFoundHandler}
import rip.deadcode.sandbox_pi.json.{JsonEncode, ScalaAdapter}

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

  implicit val moshi: Moshi = Moshi
    .Builder()
    .add(ScalaAdapter())
    .build()

  val appCtx = AppContextImpl(
    config,
    pi4j
  )

  val threadPool = QueuedThreadPool()
    .tap(_.setName("server"))
  val server = Server(threadPool)
  val connector = ServerConnector(server)
    .tap(_.setPort(config.port))
  server.addConnector(connector)

  val handlers = createHandlers(appCtx)
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
          targetHandler.handle(baseRequest, appCtx).handleError(handlerUnexpected).unsafeRunSync()
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
          logger.debug(s"Response: JSON")
          response.setContentType(MediaType.JSON_UTF_8.toString)
          response.getWriter.print(e.encode())
      }
      baseRequest.setHandled(true)
    }
  })
  server.start()
}

private def createHandlers(appCtx: AppContext)(using moshi: Moshi) = Seq(
  HelloWorldHandler(),
  PiTemperatureHandler(),
  LedHandler(appCtx),
  TemperatureHandler(appCtx)
)

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
