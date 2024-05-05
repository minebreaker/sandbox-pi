package rip.deadcode.sandbox_pi.lib

import cats.effect.IO
import com.google.common.io.CharStreams
import io.circe.Decoder
import org.eclipse.jetty.server.Request

package object circe {

  def parseJson[T](request: Request)(using decoder: Decoder[T]): IO[T] = {
    IO.fromEither {
      val inputStr = CharStreams.toString(request.getReader)
      import io.circe.syntax.*
      for {
        json <- io.circe.parser.parse(inputStr)
        input <- json.as[T]
      } yield input
    }
  }
}
