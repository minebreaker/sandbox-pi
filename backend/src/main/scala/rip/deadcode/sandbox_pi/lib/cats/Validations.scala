package rip.deadcode.sandbox_pi.lib.cats

import cats.Monoid
import cats.data.{Validated, ValidatedNel}
import cats.effect.IO
import cats.kernel.Semigroup

import java.util.UUID

object Validations {

  extension [E, A](self: ValidatedNel[E, A]) {
    def toIO: IO[A] = {
      // TODO: reconsider impls when refactor input validations
      self.leftMap(ee => Exception(ee.toList.mkString("multiple validation failures\n", "\n", ""))) match {
        case Validated.Valid(a)   => IO(a)
        case Validated.Invalid(e) => IO.raiseError(e)
      }
    }
  }

//  extension [E <: Exception, EE <: NonEmptyList[E], A](self: Validated[EE, A]) {
//    def nelToIO: IO[A] = {
//      ???
//    }
//  }

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
