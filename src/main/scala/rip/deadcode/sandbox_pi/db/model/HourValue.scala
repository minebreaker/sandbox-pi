package rip.deadcode.sandbox_pi.db.model

case class HourValue(
    average: String,
    median: String,
    max: String,
    min: String,
    year: Int,
    month: Int,
    day: Int,
    hour: Int
) extends Values
