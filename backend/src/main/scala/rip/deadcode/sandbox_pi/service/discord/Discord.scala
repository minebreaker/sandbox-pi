package rip.deadcode.sandbox_pi.service

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.Config
import rip.deadcode.sandbox_pi.build_info.BuildInfo
import rip.deadcode.sandbox_pi.pi.bm680.{Bme680, Bme680Output}
import rip.deadcode.sandbox_pi.pi.mhz19c.{Mhz19c, Mhz19cOutput}
import rip.deadcode.sandbox_pi.service.Discord.DiscordException.ErrorResponse
import rip.deadcode.sandbox_pi.service.Discord.{DiscordException, ExecuteRequest}
import rip.deadcode.sandbox_pi.service.mac.MacWatcher
import rip.deadcode.sandbox_pi.utils.{formatCo2, formatHumidity}
import sttp.client3.SttpClientException
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import sttp.model.{StatusCode, Uri}

import java.time.temporal.TemporalUnit
import java.time.{Clock, Instant}

@Singleton
class Discord @Inject() (bme680: Bme680, mhz19c: Mhz19c, macWatcher: MacWatcher, clock: Clock, config: Config) {

  private val logger = LoggerFactory.getLogger(classOf[Discord])

  private val maybeWebhookString: Option[String] = config.discord.webhook
  private val maybeWebhookUrl: Option[Uri] = maybeWebhookString.map(Uri.unsafeParse)

  private val HumLowerThreshold = 40
  private val HumUpperThreshold = 70
  private val Co2UpperThreshold = 1000

  def run(): IO[Unit] = {
    maybeWebhookUrl match {
      case Some(value) =>
        for {
          isAtHome <- IO(macWatcher.isAtHome)
          _ <- IO.whenA(isAtHome) {
            for {
              tph <- IO(bme680.getData)
              co2 <- IO(mhz19c.getData)
              _ <- sendNotification(value, tph, co2).recover {
                case e: SttpClientException =>
                  logger.warn("Failed to send webhook: Sttp error", e)
                case e: ErrorResponse =>
                  logger.warn(s"Failed to send webhook: Invalid response [code={}]", e.status)
              }
            } yield ()
          }
        } yield ()
      case None =>
        IO {
          logger.debug("Discord WebHook URL is not set.")
        }
    }
  }

  def sendStartupNotification(): IO[Unit] = {
    maybeWebhookUrl match {
      case Some(value) =>
        send(
          value,
          s"""Starting the sandbox-pi server...
             |---------------------------------
             |Version: ${BuildInfo.version}
             |Scala:   ${BuildInfo.scalaVersion}
             |Sbt:     ${BuildInfo.sbtVersion}
             |---------------------------------
             |""".stripMargin
        )
      case None =>
        IO {
          logger.debug("Discord WebHook URL is not set.")
        }
    }
  }

  private def sendNotification(webhook: Uri, tph: Bme680Output, co2: Mhz19cOutput): IO[Unit] = {
    import scala.concurrent.duration.*
    import scala.jdk.DurationConverters.*
    for {
      now <- IO { clock.instant() }
      _ <- IO.whenA(tph.hum < HumLowerThreshold) {
        send(webhook, s"Humidity is too low! (${formatHumidity(tph.hum)})")
      }
      _ <- IO.whenA(tph.hum > HumUpperThreshold) {
        send(webhook, s"Humidity is too high! (${formatHumidity(tph.hum)})")
      }
      _ <- IO.whenA(co2.co2 > Co2UpperThreshold) {
        send(webhook, s"CO2 is too high! (${formatCo2(co2.co2)})")
      }
    } yield ()
  }

  private def send(webhook: Uri, message: String): IO[Unit] = {
    import sttp.client3.circe.*
    HttpClientCatsBackend.resource[IO]().use { backend =>
      for {
        response <- sttp.client3.basicRequest
          .post(webhook)
          .body(
            ExecuteRequest(
              message
            )
          )
          .response(sttp.client3.ignore)
          .send(backend)
        _ <- IO.raiseUnless(response.code == StatusCode.NoContent)(ErrorResponse(response.code.code))
      } yield ()
    }
  }
}

object Discord {

  implicit val encoder: Encoder[ExecuteRequest] = deriveEncoder

  case class ExecuteRequest(
      content: String
  )

  sealed abstract class DiscordException(message: String = null, cause: Exception = null)
      extends RuntimeException(message, cause)

  object DiscordException {
    final case class ErrorResponse(status: Int)
        extends DiscordException(s"""A None-OK response is returned: "$status"""")
  }
}
