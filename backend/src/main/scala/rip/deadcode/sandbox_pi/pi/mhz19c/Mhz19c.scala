package rip.deadcode.sandbox_pi.pi.mhz19c

import com.google.inject.{Inject, Singleton}
import com.pi4j.context.Context as Pi4JContext
import com.pi4j.io.serial.{Baud, Serial, SerialProvider}
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.utils.*

import java.nio.ByteBuffer
import java.time.Clock
import scala.util.{Failure, Success, Try, Using}

@Singleton
class Mhz19c @Inject() (pi4j: Pi4JContext, clock: Clock) {

  private val logger = LoggerFactory.getLogger(classOf[Mhz19c])

  private val provider = pi4j.provider[SerialProvider]("pigpio-serial")
  private val config = Serial
    .newConfigBuilder(pi4j)
    .id("MH-Z19C")
    .device("/dev/serial0")
    .baud(Baud._9600)
    .dataBits_8()
    .parityNone()
    .stopBits_1()
    .build()

  private val serial = provider.create(config)

  @volatile
  private var result: Option[Mhz19cOutput] = None

  def getData: Mhz19cOutput = {
    result.getOrElse(throw new RuntimeException("Not initialized yet."))
  }

  def refresh(): Try[Mhz19cOutput] = {
    Try {
      val co2 = serial.synchronized {
        readData()
      }
      val output = Mhz19cOutput(co2, clock.instant())
      result = Some(output)
      output
    }
  }

  private def readData(): Int = {

    if (!serial.isOpen) {
      logger.debug("Tries to open serial connection.")
      serial.open()
    }

    // Read command
    serial.write(
      Array[Byte](
        0xFF.toByte,
        0x01,
        0x86.toByte,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x79
      )
    )

    var read = true
    val result = ByteBuffer.allocate(9)
    while (read) {
      val available = serial.available()
      logger.debug("available: {}, pos: {}", available, result.position())
      if (available > 0) {
        val readAmount = serial.read(result, available)
        logger.debug("read: {}{}", readAmount, show(result.array()))
        if (result.position() >= 9) {
          read = false
        }
      } else {
        Thread.sleep(100)
        logger.debug("Waiting for the data get available...")
      }
    }

    logger.debug(show(result.array()))

    processData(result)
  }

  def processData(result: ByteBuffer): Int = {

    val higher = result.get(2).toUnsigned
    val lower = result.get(3).toUnsigned

    val receivedChecksum = result.get(8).toUnsigned
    val calculatedChecksum = (0xFF - result.array().slice(1, 8).map(_.toUnsigned).sum + 1).toByte.toUnsigned

    val concentration = higher * 256 + lower
    logger.info("CO2(ppm): {}", concentration)

    if (receivedChecksum != calculatedChecksum) {
      throw new Exception(s"Checksum did not match. received: $receivedChecksum, calculated: $calculatedChecksum")
    }

    concentration
  }
}
