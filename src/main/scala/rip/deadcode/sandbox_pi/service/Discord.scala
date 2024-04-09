package rip.deadcode.sandbox_pi.service

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.Config
import rip.deadcode.sandbox_pi.pi.bm680.Bme680Output
import rip.deadcode.sandbox_pi.pi.mhz19c.Mhz19cOutput
import rip.deadcode.sandbox_pi.service.Discord.DiscordException.ErrorResponse
import rip.deadcode.sandbox_pi.service.Discord.{DiscordException, ExecuteRequest}
import rip.deadcode.sandbox_pi.utils.{formatCo2, formatHumidity}
import sttp.client3.SttpClientException
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import sttp.model.{StatusCode, Uri}

import java.time.temporal.TemporalUnit
import java.time.{Clock, Instant}
import java.util.concurrent.atomic.AtomicReference

// TODO separate the daemon runner from the discord service itself
@Singleton
class Discord @Inject() (clock: Clock, config: Config) {

  private val logger = LoggerFactory.getLogger(classOf[Discord])

  private val maybeWebhookString: Option[String] = config.discord.webhook
  private val maybeWebhookUrl: Option[Uri] = maybeWebhookString.map(Uri.unsafeParse)

  private val lastSentHumLower: AtomicReference[Instant] = AtomicReference(Instant.ofEpochSecond(0))
  private val humLowerThreshold = 40
  private val lastSentHumUpper: AtomicReference[Instant] = AtomicReference(Instant.ofEpochSecond(0))
  private val humUpperThreshold = 70
  private val lastSentCo2: AtomicReference[Instant] = AtomicReference(Instant.ofEpochSecond(0))
  private val co2UpperThreshold = 1000

  def run(tph: Bme680Output, co2: Mhz19cOutput): IO[Unit] = {
    maybeWebhookUrl match {
      case Some(value) =>
        sendNotification(value, tph, co2).recover {
          case e: SttpClientException =>
            logger.warn("Failed to send webhook: Sttp error", e)
          case e: ErrorResponse =>
            logger.warn(s"Failed to send webhook: Invalid response [code={}]", e.status)
        }
      case None => IO.unit
    }
  }

  def sendStartupNotification(): IO[Unit] = {
    maybeWebhookUrl match {
      case Some(value) => send(value, s"Starting the sandbox-pi server...")
      case None        => IO.unit
    }
  }

  private def sendNotification(webhook: Uri, tph: Bme680Output, co2: Mhz19cOutput): IO[Unit] = {
    import scala.concurrent.duration.*
    import scala.jdk.DurationConverters.*
    val now = clock.instant()
    for {
      _ <- IO.whenA(tph.hum < humLowerThreshold) {
        val lastSent = lastSentHumLower.get() // This may send notifications twice but I don't care
        if (lastSent.isBefore(now.minus(30.minutes.toJava))) {
          lastSentHumLower.set(now)
          send(webhook, s"Humidity is too low! (${formatHumidity(tph.hum)})")
        } else {
          IO {
            logger.info("Skipping to send a notification (humidity): recently sent a notification.")
          }
        }
      }
      _ <- IO.whenA(tph.hum > humUpperThreshold) {
        val lastSent = lastSentHumUpper.get()
        if (lastSent.isBefore(now.minus(30.minutes.toJava))) {
          lastSentHumUpper.set(now)
          send(webhook, s"Humidity is too high! (${formatHumidity(tph.hum)})")
        } else {
          IO {
            logger.info("Skipping to send a notification (humidity): recently sent a notification.")
          }
        }
      }
      _ <- IO.whenA(co2.co2 > co2UpperThreshold) {
        val lastSent = lastSentCo2.get()
        if (lastSent.isBefore(now.minus(30.minutes.toJava))) {
          lastSentCo2.set(now)
          send(webhook, s"CO2 is too high! (${formatCo2(co2.co2)})")
        } else {
          IO {
            logger.info("Skipping to send a notification (humidity): recently sent a notification.")
          }
        }
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
