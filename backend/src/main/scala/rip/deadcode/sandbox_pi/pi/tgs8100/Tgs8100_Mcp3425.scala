package rip.deadcode.sandbox_pi.pi.tgs8100

import com.pi4j.context.Context as Pi4JContext
import com.pi4j.io.gpio.digital.{DigitalOutput, DigitalOutputProvider, DigitalState}
import com.pi4j.io.i2c.{I2C, I2CProvider}

import scala.util.{Failure, Success, Try, Using}
import scala.util.Using.Releasable

class Tgs8100_Mcp3425 {

  private val PulseGpioPinAddr = ???
  private val I2cAddr = 0x68

  def run(pi4j: Pi4JContext): Unit = {

    val doutProvider = pi4j.dout[DigitalOutputProvider]
    val doutConfig = DigitalOutput
      .newConfigBuilder(pi4j)
      .id("led")
      .address(PulseGpioPinAddr)
      .initial(DigitalState.LOW)
      .shutdown(DigitalState.LOW)
      .build()

    val dout = doutProvider.create(doutConfig)
    implicit val closeDout: Releasable[DigitalOutput] = { r => r.shutdown(pi4j) }

    val i2cProvider = pi4j.i2c[I2CProvider]
    val config = I2C
      .newConfigBuilder(pi4j)
      .id("BME680")
      .bus(1)
      .device(I2cAddr)
      .build()

    val i2c = i2cProvider.create(config)

    Using.resources(dout, i2c) { (dout, i2c) =>
      Try {
        // Send pulse to begin analysis
        dout.high()

        // begin one-shot conversion
        i2c.write(0.toByte) // 10000000
        val result = i2c.readByteBuffer(3)
        dout.low()

        val outputCode = result.position(0).getShort & 159 // 159 = 10011111, remove unwanted MSB
        val lsb = 0.001
        val voltage = outputCode * lsb

      } match {
        // Turn of pulse
        case Success(value) =>
          dout.low()
        case Failure(exception) =>
          dout.low()
      }
    }
  }
}
