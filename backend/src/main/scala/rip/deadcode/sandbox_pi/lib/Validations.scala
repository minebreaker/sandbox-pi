package rip.deadcode.sandbox_pi.lib

import cats.data.{Validated, ValidatedNel}

import java.util.UUID

object Validations {

  def validateOptionInt(i: Option[String], param: => String): ValidatedNel[String, Option[Int]] = {
    i match {
      case Some(i) => Validated.fromOption(i.toIntOption, param).map(Some(_)).toValidatedNel
      case None    => Validated.validNel(None)
    }
  }

  def validateUuid(s: Option[String], param: => String): ValidatedNel[String, UUID] = {
    s match {
      case Some(value) =>
        Validated
          .catchNonFatal {
            UUID.fromString(value)
          }
          .leftMap(_ => param)
          .toValidatedNel
      case None => Validated.invalidNel(param)
    }
  }
}
