package rip.deadcode.sandbox_pi.utils

extension (self: Seq[Double]) {

  def avg: Double = {
    self.sum / self.length
  }

  def med: Double = {
    val sorted = self.sorted
    val pos = self.length / 2

    if (self.length % 2 == 0) {
      sorted(pos)
    } else {
      (sorted(pos) + sorted(pos + 1)) / 2
    }
  }
}
