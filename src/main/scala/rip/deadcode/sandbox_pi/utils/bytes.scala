package rip.deadcode.sandbox_pi.utils

import com.google.common.base.Strings

import scala.util.Properties

def show(byte: Byte): String = {
  val hex = "0x" + Strings.padStart(Integer.toHexString(java.lang.Byte.toUnsignedInt(byte)).toUpperCase, 2, '0')
  val bits = showBits(byte)
  s"$hex $bits - $byte"
}

def show(bytes: Array[Byte]): String = {
  bytes.zipWithIndex
    .map { (byte, i) =>
      String.format("%02d  %s", i, show(byte))
    }
    .mkString("\n", Properties.lineSeparator, "")
}

def showBits(byte: Byte): String = {
  val result = new StringBuilder(8)
  for (i <- 7 to 0 by -1) {
    val bit = (byte >> i) & 1
    result.append(bit)
  }
  result.toString
}
