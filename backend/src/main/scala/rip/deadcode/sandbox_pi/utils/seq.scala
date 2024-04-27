package rip.deadcode.sandbox_pi.utils

extension (self: Seq[Double]) {

  def avg: Double = {
    if (self.isEmpty) {
      return Double.NaN
    }
    self.sum / self.length
  }

  def med: Double = {
    if (self.isEmpty) {
      return Double.NaN
    }
    if (self.size == 1) {
      return self.head
    }

    val sorted = self.sorted
    val pos = self.length / 2

    if (self.length % 2 == 0) {
      sorted(pos)
    } else {
      (sorted(pos) + sorted(pos + 1)) / 2
    }
  }
}
