package rip.deadcode.sandbox_pi.pi.bm680

import com.google.inject.{Inject, Singleton}
import com.pi4j.context.Context as Pi4JContext
import com.pi4j.io.i2c.{I2C, I2CProvider}
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.pi.bm680.Device.Data
import rip.deadcode.sandbox_pi.utils.*

import java.nio.ByteBuffer
import java.time.Clock
import java.util.concurrent.atomic.AtomicReference
import scala.util.Try

@Singleton
class Bme680 @Inject() (pi4j: Pi4JContext, clock: Clock) {

  private val device = new Device(pi4j)

  @volatile
  private var result: Option[Bme680Output] = None

  def getData: Bme680Output = {
    result.getOrElse(throw new RuntimeException("Not initialized yet."))
  }

  def refresh(): Try[Bme680Output] = {
    Try {
      val data = device.synchronized {
        device.readData()
      }
      val output = Bme680Output(
        data.temp,
        data.press,
        data.hum,
        clock.instant()
      )
      result = Some(output)
      output
    }
  }
}

private[bm680] class Device(pi4j: Pi4JContext) {

  private val logger = LoggerFactory.getLogger(classOf[Bme680])

  private val Bme680I2cDeviceAddr = 0x77

  private val provider = pi4j.provider[I2CProvider]("pigpio-i2c")
  private val config = I2C
    .newConfigBuilder(pi4j)
    .id("BME680")
    .bus(1)
    .device(Bme680I2cDeviceAddr)
    .build()
  private val i2c = provider.create(config)

  private val CtrlGas1Addr = 0x71
  private val CtrlHumAddr = 0x72
  private val CtrlMeasAddr = 0x74

  private var parT1: Double = 0
  private var parT2: Double = 0
  private var parT3: Double = 0

  private var parP1: Double = 0
  private var parP2: Double = 0
  private var parP3: Double = 0
  private var parP4: Double = 0
  private var parP5: Double = 0
  private var parP6: Double = 0
  private var parP7: Double = 0
  private var parP8: Double = 0
  private var parP9: Double = 0
  private var parP10: Double = 0

  private var parH1: Double = 0
  private var parH2: Double = 0
  private var parH3: Double = 0
  private var parH4: Double = 0
  private var parH5: Double = 0
  private var parH6: Double = 0
  private var parH7: Double = 0

  // initialization
  {
    val id = i2c.readRegisterByte(0xD0)
    logger.info(show(id))
    readCalibrationData()
  }

  def readCalibrationData(): Unit = {
    // Temperature calibration
    val parT1B = i2c.readRegisterByteBuffer(0xE9, 2)
    val parT2T3B = i2c.readRegisterByteBuffer(0x8A, 3)
    parT1 = ByteBuffer.allocate(2).put(parT1B.get(1)).put(parT1B.get(0)).getShort(0).toUnsignedDouble
    parT2 = ByteBuffer.allocate(2).put(parT2T3B.get(1)).put(parT2T3B.get(0)).getShort(0).toDouble
    parT3 = parT2T3B.get(2).toDouble

    logger.debug(show(parT1B.array()))
    logger.debug(show(parT2T3B.array()))
    logger.debug("par_t1 {}", parT1)
    logger.debug("par_t2 {}", parT2)
    logger.debug("par_t3 {}", parT3)

    // Pressure calibration
    // Read from 0x82 to 0xA0
    val parPB = i2c.readRegisterByteBuffer(0x8E, 19)
    parP1 = ByteBuffer.allocate(4).position(2).put(parPB.get(1)).put(parPB.get(0)).getInt(0).toUnsignedDouble
    parP2 = ByteBuffer.allocate(2).put(parPB.get(3)).put(parPB.get(2)).getShort(0).toDouble
    parP3 = parPB.get(4).toDouble
    parP4 = ByteBuffer.allocate(2).put(parPB.get(7)).put(parPB.get(6)).getShort(0).toDouble
    parP5 = ByteBuffer.allocate(2).put(parPB.get(9)).put(parPB.get(8)).getShort(0).toDouble
    parP6 = parPB.get(11).toDouble
    parP7 = parPB.get(10).toDouble
    parP8 = ByteBuffer.allocate(2).put(parPB.get(15)).put(parPB.get(14)).getShort(0).toDouble
    parP9 = ByteBuffer.allocate(2).put(parPB.get(17)).put(parPB.get(16)).getShort(0).toDouble
    parP10 = parPB.get(18).toUnsignedDouble

    logger.debug(show(parPB.array()))
    logger.debug("par_p1 {}", parP1)
    logger.debug("par_p2 {}", parP2)
    logger.debug("par_p3 {}", parP3)
    logger.debug("par_p4 {}", parP4)
    logger.debug("par_p5 {}", parP5)
    logger.debug("par_p6 {}", parP6)
    logger.debug("par_p7 {}", parP7)
    logger.debug("par_p8 {}", parP8)
    logger.debug("par_p9 {}", parP9)
    logger.debug("par_p10 {}", parP10)

    val parHB = i2c.readRegisterByteBuffer(0xE1, 8)
    val parH1B = ByteBuffer.allocate(2).put(parHB.get(2)).put((parHB.get(1) << 4).toByte)
    parH1 = (parH1B.getShort(0) >> 4).toUnsignedDouble
    val parH2B = ByteBuffer.allocate(2).put(parHB.get(0)).put((parHB.get(1) << 4).toByte)
    parH2 = (parH2B.getShort(0) >> 4).toUnsignedDouble
    parH3 = parHB.get(3).toDouble
    parH4 = parHB.get(4).toDouble
    parH5 = parHB.get(5).toDouble
    parH6 = parHB.get(6).toUnsignedDouble
    parH7 = parHB.get(7).toDouble

    logger.debug(show(parHB.array()))
    logger.debug("par_h1 {}", parH1)
    logger.debug("par_h2 {}", parH2)
    logger.debug("par_h3 {}", parH3)
    logger.debug("par_h4 {}", parH4)
    logger.debug("par_h5 {}", parH5)
    logger.debug("par_h6 {}", parH6)
    logger.debug("par_h7 {}", parH7)
  }

  def readData(): Data = {

    // The values cast to double must be treated as unsigned
    // The doc doesn't mention which params are signed/unsigned, so you should look at the API implementation to check out the actual types.

    // FIXME: bulk write???
    // Ctrl_hum: 0x72, Ctrl_meas: 0x74, Config: 0x75; recommended to write at once
//    i2c.writeRegister(
//      CtrlHum,
//      Array[Byte](
//        0x1, // Set osrs_h x1,
//        0x0,
//        (0x2 << 5) | (0x1 << 2) | 0x1, // Set osrs_t x1, osrs_p x1, force mode
////        0x01 << 2 // Set IIR filter
//        0x0 // Disable IIR filter
//      )
//    )
// TODO set IIR filter

    i2c.writeRegister(CtrlHumAddr, 0x1) // Set osrs_h x1
    i2c.writeRegister(CtrlMeasAddr, (0x1 << 5) | (0x1 << 2) | 0x1) // Set osrs_t x1, osrs_p x1, force mode

    logger.debug("Ctrl" + show(i2c.readRegisterByteBuffer(CtrlGas1Addr, 4).array()))

//    Thread.sleep(100) // Gas heating time
    var done = false
    while (!done) {
      Thread.sleep(10)
      val state = i2c.readRegisterByte(CtrlMeasAddr) & 0x1
      logger.debug("Mode: " + state.toString)
      if (state == 0) {
        done = true
      }
    }

    // TODO: register bulk read

    val (tempComp, tFine) = {
      val tempAdcB = ByteBuffer.allocate(4) // For easier read, allocates 4 bytes
      i2c.readRegister(0x22, tempAdcB, 3)

      val tempAdc = (tempAdcB.getInt(0) >> 12).toUnsignedDouble
      logger.debug("temp_adc" + show(tempAdcB.array()))
      logger.debug(tempAdc.toString)

      val var1 = ((tempAdc / 16384) - (parT1 / 1024)) * parT2
      val var2 = (((tempAdc / 131072) - (parT1 / 8192)) * ((tempAdc / 131072) - parT1 / 8192)) * (parT3 * 16)
      val tFine = var1 + var2
      val tempComp = tFine / 5120
      (tempComp, tFine)
    }

    val pressComp = {
      val pressAdcB = ByteBuffer.allocate(4)
      i2c.readRegister(0x1F, pressAdcB, 3)

      val pressAdc = (pressAdcB.getInt(0) >> 12).toUnsignedDouble
      logger.debug("press_adc" + show(pressAdcB.array()))
      logger.debug(pressAdc.toString)

      var var1 = (tFine / 2) - 64000
      var var2 = var1 * var1 * (parP6 / 131072)
      var2 = var2 + (var1 * parP5 * 2)
      var2 = (var2 / 4) + (parP4 * 65536)
      var1 = (((parP3 * var1 * var1) / 16384) + (parP2 * var1)) / 524288
      var1 = (1 + (var1 / 32768)) * parP1
      var pressComp = 1048576 - pressAdc
      if (var1 == 0) {
        0
      } else {
        pressComp = ((pressComp - (var2 / 4096)) * 6250) / var1
        var1 = (parP9 * pressComp * pressComp) / 2147483648d
        var2 = pressComp * (parP8 / 32768)
        val var3 = (pressComp / 256) * (pressComp / 256) * (pressComp / 256) * (parP10 / 131072)
        pressComp = pressComp + ((var1 + var2 + var3 + (parP7 * 128)) / 16)

        pressComp
      }
    }

    val humComp = {
      val humAdcB = i2c.readRegisterByteBuffer(0x25, 2)

      val humAdc = humAdcB.getShort(0).toUnsignedDouble
      logger.debug("hum_adc" + show(humAdcB.array()))
      logger.debug(humAdc.toString)

      val var1 = humAdc - ((parH1 * 16) + ((parH3 / 2) * tempComp))
      val var2 = var1 * (
        (parH2 / 262144) * (1 + ((parH4 / 16384) * tempComp) + ((parH5 / 1048576) * tempComp * tempComp))
      )
      val var3 = parH6 / 16384
      val var4 = parH7 / 2097152
      val humComp = var2 + ((var3 + (var4 * tempComp)) * var2 * var2)

      humComp
    }

    logger.info("Temp(C):   {}", String.format("%.8f", tempComp))
    // 1000hpa = 100000pa
    logger.info("Press(Pa): {}", String.format("%.8f", pressComp))
    logger.info("Hum(%):    {}", String.format("%.8f", humComp))

    if (tempComp >= 50 || tempComp <= -20) {
      throw new RuntimeException(s"Data integrity check failed: Unlikely temperature value: $tempComp")
    }
    if (pressComp >= 120_000 || pressComp <= 80_000) {
      throw new RuntimeException(s"Data integrity check failed: Unlikely pressure value: $pressComp")
    }
    if (humComp > 100 || humComp < 0) {
      throw new RuntimeException(s"Data integrity check failed: Unlikely humidity value: $humComp")
    }

    Data(tempComp, pressComp, humComp)
  }
}

object Device {

  case class Data(
      temp: Double,
      press: Double,
      hum: Double
  )

}
