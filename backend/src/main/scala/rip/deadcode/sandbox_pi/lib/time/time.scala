package rip.deadcode.sandbox_pi.lib.time

import java.time.{Instant, ZoneId, ZonedDateTime}


def timestampToTime(ts: Instant, zoneId: ZoneId) = {
  val dt = ZonedDateTime.ofInstant(ts, zoneId)
  (dt, dt.getYear, dt.getMonthValue, dt.getDayOfMonth, dt.getHour, dt.getMinute)
}
