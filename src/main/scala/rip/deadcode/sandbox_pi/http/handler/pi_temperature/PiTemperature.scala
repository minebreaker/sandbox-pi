package rip.deadcode.sandbox_pi.http.handler.pi_temperature

import cats.effect.{IO, Resource}
import org.slf4j.LoggerFactory

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.concurrent.Future
import scala.sys.process.*

object PiTemperature {

  private val logger = LoggerFactory.getLogger(classOf[PiTemperature.type])
  private val TempPath = Path.of("/sys/class/thermal/thermal_zone0/temp")

  def run(): IO[PiTemperatureOutput] = {
    val f = for {
      tempStr <- IO.blocking {
        String(Files.readAllBytes(TempPath), StandardCharsets.UTF_8).replaceAll("[\r\n]", "")
      }
    } yield {
      logger.debug(s"raw: $tempStr")
      val i = tempStr.length - 3
      val tempUpper = tempStr.substring(0, i)
      val tempLower = tempStr.substring(i, i + 1)
      val tempReadable = tempUpper + "." + tempLower + "℃"
      PiTemperatureOutput(tempStr.toInt, tempReadable)
    }
    f.recoverWith {
      case e: IOException =>
        logger.info("", e)
        ???
      case e: IndexOutOfBoundsException =>
        logger.info("", e)
        ???
      case e: NumberFormatException =>
        logger.info("", e)
        ???
    }
  }
}
