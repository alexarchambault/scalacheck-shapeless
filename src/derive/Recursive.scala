package org.scalacheck.derive

import org.scalacheck.Gen

sealed abstract class Recursive[T] {
  /**
    * If scalacheck's size parameter prevents generating an arbitrary `T`, generate a value
    * with this `Gen[T]` instead.
    */
  def default: Gen[T]
}

object Recursive {
  /**
    * Flags type `T` as recursive. Resulting value should be marked as implicit.
    *
    * Makes the generation of recursive type instances deterministic: these don't fail on
    * `StackOverflowError`, but via scalacheck's size parameter. In that case, `default0` is used,
    * and can either decide to fail or generate fallback values.
    */
  def apply[T](default0: Gen[T] = Gen.fail): Recursive[T] =
    new Recursive[T] {
      def default = default0
    }

  case class Value[+T](valueOpt: Option[T]) extends AnyVal {
    def map[U](f: T => U): Value[U] =
      Value(valueOpt.map(f))
  }
}