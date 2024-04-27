package rip.deadcode.sandbox_pi.http

import com.google.common.net.MediaType
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

sealed trait HttpResponse {
  val status: Int
  val header: Map[String, String]
}

object HttpResponse {

  case class StringHttpResponse(
      status: Int,
      contentType: MediaType,
      body: String,
      header: Map[String, String] = Map.empty
  ) extends HttpResponse

  case class JsonHttpResponse[T](
      status: Int,
      body: T,
      header: Map[String, String] = Map.empty
  )(implicit encoder: Encoder[T])
      extends HttpResponse {

    def encode: String = {
      import io.circe.syntax.*
      body.asJson.noSpaces
    }
  }

  object JsonHttpResponse {

    def invalidParameter(param: String): JsonHttpResponse[InvalidParameter] = JsonHttpResponse(
      400,
      InvalidParameter(param)
    )

    def unknownError(message: String): JsonHttpResponse[ErrorResponse] = JsonHttpResponse(
      500,
      ErrorResponse(message)
    )

    implicit val encodeInvalidParameter: Encoder[InvalidParameter] = deriveEncoder
    implicit val encoderError: Encoder[ErrorResponse] = deriveEncoder

    case class InvalidParameter(message: String)
    case class ErrorResponse(message: String)
  }
}
