package org.scalacheck.derive

import org.scalacheck.Gen

object GenExtra {

  implicit class GenOps[T](val gen: Gen[T]) extends AnyVal {
    def getOrFail[U](implicit ev: T <:< Option[U]): Gen[U] =
      gen.flatMap {
        ev(_) match {
          case None => Gen.fail
          case Some(u) => Gen.const(u)
        }
      }
  }

  def failOnStackOverflow[T](gen: Gen[T]): Gen[T] =
    Gen.gen { (p, seed) =>
      try gen.doApply(p, seed)
      catch {
        // likely not fine on Scala JS
        case _: StackOverflowError =>
          Gen.r(None, seed.next)
      }
    }

}