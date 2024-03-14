package rip.deadcode.sandbox_pi.http

import com.google.common.net.MediaType
import rip.deadcode.sandbox_pi.json.JsonEncode

sealed trait HttpResponse {
  val status: Int
  val header: Map[String, String]
}

object HttpResponse {

  case class JsonHttpResponse[T: JsonEncode](
      status: Int,
      body: T,
      header: Map[String, String] = Map.empty
  ) extends HttpResponse {
    def encode(): String = body.encode()
  }
}
