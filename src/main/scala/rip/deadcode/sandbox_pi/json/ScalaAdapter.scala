package rip.deadcode.sandbox_pi.json

import com.squareup.moshi.{JsonAdapter, JsonWriter, ToJson}

class ScalaAdapter {

  @ToJson
  def seq[T](writer: JsonWriter, seq: Seq[T], delegate: JsonAdapter[T]): Unit = {
    writer.beginArray()
    seq.foreach { e =>
      delegate.toJson(e)
    }
    writer.endArray()
  }
}
