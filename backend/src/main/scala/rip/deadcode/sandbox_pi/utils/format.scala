package rip.deadcode.sandbox_pi.utils

private def toDouble(v: String | Double | Int) = {
  v match {
    case v: String => v.toDouble
    case v: Double => v
    case v: Int    => v.toDouble
  }
}

def formatTemperature(temperature: String | Double) = {
  String.format("%.1f ℃", toDouble(temperature))
}

def formatPressure(pressure: String | Double) = {
  String.format("%f ㍱", toDouble(pressure) / 100)
}

def formatHumidity(humidity: String | Double) = {
  String.format("%.1f %%", toDouble(humidity))
}

def formatCo2(co2: String | Double | Int) = {
  String.format("%.0f ㏙", toDouble(co2))
}
