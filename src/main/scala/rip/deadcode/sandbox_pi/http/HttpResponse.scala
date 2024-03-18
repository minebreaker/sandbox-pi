package rip.deadcode.sandbox_pi.http

import com.google.common.net.MediaType
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

sealed trait HttpResponse {
  val status: Int
  val header: Map[String, String]
}

object HttpResponse {

  case class JsonHttpResponse[T](
      status: Int,
      body: T,
      header: Map[String, String] = Map.empty
  )(implicit encoder: Encoder[T])
      extends HttpResponse {

    def encode: String = {
      import io.circe.syntax._
      body.asJson.noSpaces
    }
  }

  object JsonHttpResponse {

    def invalidParameter(param: String): JsonHttpResponse[InvalidParameter] = JsonHttpResponse(
      400,
      InvalidParameter(param)
    )

    case class InvalidParameter(reason: String)

    object InvalidParameter {
      implicit val encoder: Encoder[InvalidParameter] = deriveEncoder
    }
  }
}
