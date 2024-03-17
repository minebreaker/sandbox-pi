package rip.deadcode.sandbox_pi.db.model

case class MonthValue(
    average: String,
    median: String,
    max: String,
    min: String,
    year: Int,
    month: Int
) extends Values
