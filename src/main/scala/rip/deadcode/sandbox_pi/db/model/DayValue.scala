package rip.deadcode.sandbox_pi.db.model

case class DayValue(
    average: String,
    median: String,
    max: String,
    min: String,
    year: Int,
    month: Int,
    day: Int
) extends Values
