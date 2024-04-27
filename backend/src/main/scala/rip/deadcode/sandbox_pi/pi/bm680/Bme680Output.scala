package rip.deadcode.sandbox_pi.pi.bm680

import java.time.Instant

case class Bme680Output(
    temp: Double,
    press: Double,
    hum: Double,
    timestamp: Instant
)
