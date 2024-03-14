package rip.deadcode.sandbox_pi.pi.mhz19c

import com.pi4j.context.Context as Pi4JContext
import com.pi4j.io.serial.{Baud, Serial, SerialProvider}
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.utils.show

import java.nio.ByteBuffer
import scala.util.Using

class Mhz19c(pi4j: Pi4JContext) {

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

  def run(): Unit = {

    logger.debug("Tries to open serial connection.")
    serial.open()
    while (!serial.isOpen) {
      Thread.sleep(250)
      logger.debug("Not open yet.")
    }

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
        Thread.sleep(250)
        logger.debug("Waiting for the data get available...")
      }
    }

    logger.debug(show(result.array()))

    val higher = result.get(2) & 0xFF
    val lower = result.get(3) & 0xFF

    val receivedChecksum = result.get(8) & 0xFF
    val calculatedChecksum = (0xFF - result.array().slice(1, 8).map(_ & 0xFF).sum + 1) & 0xFF

    val concentration = higher * 256 + lower
    logger.info("CO2(ppm): {}", concentration)
    logger.info("Checksum received: {}, calculated: {}", receivedChecksum, calculatedChecksum)

    serial.close()
  }
}
