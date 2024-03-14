package rip.deadcode.sandbox_pi.json

import com.squareup.moshi.Moshi

import scala.reflect.ClassTag

trait JsonEncode[T] {

  extension (self: T) {
    def encode(): String
  }
}
