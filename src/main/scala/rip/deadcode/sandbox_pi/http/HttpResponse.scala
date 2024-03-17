package rip.deadcode.sandbox_pi.http

import com.google.common.net.MediaType
import com.squareup.moshi.Moshi
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

  object JsonHttpResponse {

    def invalidParameter(param: String)(using moshi: Moshi): JsonHttpResponse[InvalidParameter] = JsonHttpResponse(
      400,
      InvalidParameter(param)
    )

    case class InvalidParameter(reason: String)

    private object InvalidParameter {
      given (using moshi: Moshi): JsonEncode[InvalidParameter] with {
        extension (self: InvalidParameter) {
          override def encode(): String = moshi.adapter(classOf[InvalidParameter]).toJson(self)
        }
      }
    }
  }
}
