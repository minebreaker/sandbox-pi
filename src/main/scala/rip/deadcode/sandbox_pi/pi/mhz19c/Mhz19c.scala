package rip.deadcode.sandbox_pi.pi.mhz19c

import com.pi4j.context.Context as Pi4JContext
import com.pi4j.io.serial.{Serial, SerialProvider}

import scala.util.Using

class Mhz19c {

  def run(pi4j: Pi4JContext): Unit = {

    val provider = pi4j.serial[SerialProvider]
    val config = Serial
      .newConfigBuilder(pi4j)
      .id("MH-Z19C")
      .use_9600_N81()
      .dataBits_8()
      .parityNone()
      .stopBits_1()
      .device(???)
      .build()

    Using(provider.create(config)) { serial =>
      serial.open()
      while (!serial.isOpen) {
        Thread.sleep(250)
      }
    }
  }
}
