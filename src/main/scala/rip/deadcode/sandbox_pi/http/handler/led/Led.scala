package rip.deadcode.sandbox_pi.http.handler.led

import cats.effect.IO
import com.pi4j.context.Context as Pi4JContext
import com.pi4j.io.gpio.digital.{DigitalOutput, DigitalOutputProvider, DigitalState}
import rip.deadcode.sandbox_pi.Scheduler
import rip.deadcode.sandbox_pi.http.handler.led.Led.GpioPinAddr

import scala.util.Using
import scala.util.Using.Releasable

class Led(pi4j: Pi4JContext) {

  private val dout: DigitalOutput = {

    val doutProvider = pi4j.provider[DigitalOutputProvider]("pigpio-digital-output")
    val config = DigitalOutput
      .newConfigBuilder(pi4j)
      .id("led")
      .address(GpioPinAddr)
      .initial(DigitalState.LOW)
      .shutdown(DigitalState.LOW)
      .build()

    doutProvider.create(config)
  }

  def run(): IO[Unit] = {
    IO.blocking {
      Scheduler.executor.execute { () =>
        {
          implicit val closeDout: Releasable[DigitalOutput] = { r => r.shutdown(pi4j) }
          Using(dout) { dout =>
            dout.high()
            Thread.sleep(5000)
            dout.low()
          }
        }
      }
    }
  }
}

object Led {
  private val GpioPinAddr = 17
}
