package rip.deadcode.sandbox_pi.pi.bm680

import com.google.inject.{Inject, Singleton}
import com.pi4j.context.Context as Pi4JContext
import com.pi4j.io.i2c.{I2C, I2CProvider}
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.utils._

import java.nio.ByteBuffer

@Singleton
class Bme680 @Inject() (pi4j: Pi4JContext) {

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

  private val CtrlGas1 = 0x71
  private val CtrlHum = 0x72
  private val CtrlMeas = 0x74

  def run(): Unit = {
    val id = i2c.readRegisterByte(0xD0)
    logger.info(show(id))

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

    i2c.writeRegister(CtrlHum, 0x1) // Set osrs_h x1
    i2c.writeRegister(CtrlMeas, (0x1 << 5) | (0x1 << 2) | 0x1) // Set osrs_t x1, osrs_p x1, force mode

    logger.info("Ctrl" + show(i2c.readRegisterByteBuffer(CtrlGas1, 4).array()))

//    Thread.sleep(100) // Gas heating time
    var done = false
    while (!done) {
      Thread.sleep(10)
      val state = i2c.readRegisterByte(CtrlMeas) & 0x1
      logger.debug("Mode: " + state.toString)
      if (state == 0) {
        done = true
      }
    }

    // TODO: register bulk read

    val (tempComp, tFine) = {
      val tempAdcB = ByteBuffer.allocate(4) // For easier read, allocates 4 bytes
      i2c.readRegister(0x22, tempAdcB, 3)
      val parT1B = i2c.readRegisterByteBuffer(0xE9, 2)
      val parT2T3B = i2c.readRegisterByteBuffer(0x8A, 3)

      val tempAdc = (tempAdcB.getInt(0) >> 12).toUnsignedDouble
      val parT1 = ByteBuffer.allocate(2).put(parT1B.get(1)).put(parT1B.get(0)).getShort(0).toUnsignedDouble
      val parT2 = ByteBuffer.allocate(2).put(parT2T3B.get(1)).put(parT2T3B.get(0)).getShort(0).toDouble
      val parT3 = parT2T3B.get(2).toDouble

      logger.debug("temp_adc" + show(tempAdcB.array()))
      logger.debug(show(parT1B.array()))
      logger.debug(show(parT2T3B.array()))
      logger.debug(tempAdc.toString)
      logger.debug(parT1.toString)
      logger.debug(parT2.toString)
      logger.debug(parT3.toString)

      val var1 = ((tempAdc / 16384) - (parT1 / 1024)) * parT2
      val var2 = (((tempAdc / 131072) - (parT1 / 8192)) * ((tempAdc / 131072) - parT1 / 8192)) * (parT3 * 16)
      val tFine = var1 + var2
      val tempComp = tFine / 5120
      (tempComp, tFine)
    }

    val pressComp = {
      val pressAdcB = ByteBuffer.allocate(4)
      i2c.readRegister(0x1F, pressAdcB, 3)
      // Read from 0x82 to 0xA0
      val parPB = i2c.readRegisterByteBuffer(0x8E, 19)

      val pressAdc = (pressAdcB.getInt(0) >> 12).toUnsignedDouble
      val parP1 = ByteBuffer.allocate(4).position(2).put(parPB.get(1)).put(parPB.get(0)).getInt(0).toUnsignedDouble
      val parP2 = ByteBuffer.allocate(2).put(parPB.get(3)).put(parPB.get(2)).getShort(0).toDouble
      val parP3 = parPB.get(4).toDouble
      val parP4 = ByteBuffer.allocate(2).put(parPB.get(7)).put(parPB.get(6)).getShort(0).toDouble
      val parP5 = ByteBuffer.allocate(2).put(parPB.get(9)).put(parPB.get(8)).getShort(0).toDouble
      val parP6 = parPB.get(11).toDouble
      val parP7 = parPB.get(10).toDouble
      val parP8 = ByteBuffer.allocate(2).put(parPB.get(15)).put(parPB.get(14)).getShort(0).toDouble
      val parP9 = ByteBuffer.allocate(2).put(parPB.get(17)).put(parPB.get(16)).getShort(0).toDouble
      val parP10 = parPB.get(18).toUnsignedDouble

      logger.debug("press_adc" + show(pressAdcB.array()))
      logger.debug(show(parPB.array()))
      logger.debug(pressAdc.toString)
      logger.debug(parP1.toString)
      logger.debug(parP2.toString)
      logger.debug(parP3.toString)
      logger.debug(parP4.toString)
      logger.debug(parP5.toString)
      logger.debug(parP6.toString)
      logger.debug(parP7.toString)
      logger.debug(parP8.toString)
      logger.debug(parP9.toString)
      logger.debug(parP10.toString)

      var var1 = (tFine / 2) - 64000
      var var2 = var1 * var1 * (parP6 / 131072)
      var2 = var2 + (var1 * parP5 * 2)
      var2 = (var2 / 4) + (parP4 * 65536)
      var1 = (((parP3 * var1 * var1) / 16384) + (parP2 * var1)) / 524288
      var1 = (1 + (var1 / 32768)) * parP1
      var pressComp = 1048576 - pressAdc
//     FIXME: if (var1 == 0) ...
      pressComp = ((pressComp - (var2 / 4096)) * 6250) / var1
      var1 = (parP9 * pressComp * pressComp) / 2147483648d
      var2 = pressComp * (parP8 / 32768)
      val var3 = (pressComp / 256) * (pressComp / 256) * (pressComp / 256) * (parP10 / 131072)
      pressComp = pressComp + ((var1 + var2 + var3 + (parP7 * 128)) / 16)

      pressComp
    }
    // 1000hpa = 100000pa

    val humComp = {
      val humAdcB = i2c.readRegisterByteBuffer(0x25, 2)
      val parHB = i2c.readRegisterByteBuffer(0xE1, 8)

      val humAdc = humAdcB.getShort(0).toUnsignedDouble
      val parH1B = ByteBuffer.allocate(2).put(parHB.get(2)).put((parHB.get(1) << 4).toByte)
      val parH1 = (parH1B.getShort(0) >> 4).toUnsignedDouble
      val parH2B = ByteBuffer.allocate(2).put(parHB.get(0)).put((parHB.get(1) << 4).toByte)
      val parH2 = (parH2B.getShort(0) >> 4).toUnsignedDouble
      val parH3 = parHB.get(3).toDouble
      val parH4 = parHB.get(4).toDouble
      val parH5 = parHB.get(5).toDouble
      val parH6 = parHB.get(6).toUnsignedDouble
      val parH7 = parHB.get(7).toDouble

      logger.debug("hum_adc" + show(humAdcB.array()))
      logger.debug(show(parHB.array()))
      logger.debug(humAdc.toString)
      logger.debug(parH1.toString)
      logger.debug(parH2.toString)
      logger.debug(parH3.toString)
      logger.debug(parH4.toString)
      logger.debug(parH5.toString)
      logger.debug(parH6.toString)
      logger.debug(parH7.toString)

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
    logger.info("Press(Pa): {}", String.format("%.8f", pressComp))
    logger.info("Hum(%):    {}", String.format("%.8f", humComp))
  }
}
