package org.scalacheck
package derive

import shapeless._
import GenExtra._

/**
 * Derives `Arbitrary[T]` instances for `T` an `HList`, a `Coproduct`,
 * a case class or an ADT (or more generally, a type represented
 * `Generic`ally as an `HList` or a `Coproduct`).
 *
 * Use like
 *     val arbitrary: Arbitrary[T] = MkArbitrary[T].arbitrary
 * or look up for an implicit `MkArbitrary[T]`.
 */
trait MkArbitrary[T] {
  /** `Arbitrary[T]` instance built by this `MkArbitrary[T]` */
  def arbitrary: Arbitrary[T]
}

abstract class MkArbitraryLowPriority {

  implicit def genericNonRecursiveCoproduct[S, C <: Coproduct]
   (implicit
     gen: Generic.Aux[S, C],
     mkArb: Lazy[MkCoproductArbitrary[C]]
   ): MkArbitrary[S] =
    MkArbitrary.instance(
      Arbitrary(
        Gen.lzy(mkArb.value.arbitrary.arbitrary)
          .map(gen.from)
          // see the discussion in https://github.com/alexarchambault/scalacheck-shapeless/issues/50
          .failOnStackOverflow
      )
    )
}

object MkArbitrary extends MkArbitraryLowPriority {
  def apply[T](implicit mkArb: MkArbitrary[T]): MkArbitrary[T] = mkArb

  def instance[T](arb: => Arbitrary[T]): MkArbitrary[T] =
    new MkArbitrary[T] {
      def arbitrary = arb
    }

  implicit def genericProduct[P, L <: HList]
   (implicit
     gen: Generic.Aux[P, L],
     mkArb: Lazy[MkHListArbitrary[L]]
   ): MkArbitrary[P] =
    instance(
      Arbitrary(Gen.lzy(mkArb.value.arbitrary.arbitrary).map(gen.from))
    )

  implicit def genericRecursiveCoproduct[S, C <: Coproduct]
   (implicit
     rec: Recursive[S],
     gen: Generic.Aux[S, C],
     mkArb: Lazy[MkRecursiveCoproductArbitrary[C]]
   ): MkArbitrary[S] =
    instance(
      Arbitrary(
        Gen.lzy(mkArb.value.arbitrary.arbitrary)
          .flatMap {
            _.valueOpt match {
              case None =>
                rec.default
              case Some(c) =>
                Gen.const(gen.from(c))
            }
          }
      )
    )

  @deprecated("Kept for binary compatibility purposes only.", "1.1.7")
  def genericCoproduct[S, C <: Coproduct](
    gen: Generic.Aux[S, C],
    mkArb: Lazy[MkCoproductArbitrary[C]]
  ): MkArbitrary[S] =
    instance(
      Arbitrary(
        Gen.lzy(mkArb.value.arbitrary.arbitrary)
          .map(gen.from)
      )
    )
}


trait MkHListArbitrary[L <: HList] {
  /** `Arbitrary[T]` instance built by this `MkArbitraryHList[T]` */
  def arbitrary: Arbitrary[L]
}

object MkHListArbitrary {
  def apply[L <: HList](implicit mkArb: MkHListArbitrary[L]): MkHListArbitrary[L] = mkArb

  def instance[L <: HList](arb: => Arbitrary[L]): MkHListArbitrary[L] =
    new MkHListArbitrary[L] {
      def arbitrary = arb
    }

  implicit val hnil: MkHListArbitrary[HNil] =
    instance(Arbitrary(Gen.const(HNil)))

  implicit def hcons[H, T <: HList, N <: Nat]
   (implicit
     headArbitrary: Strict[Arbitrary[H]],
     tailArbitrary: MkHListArbitrary[T],
     length: ops.hlist.Length.Aux[T, N],
     n: ops.nat.ToInt[N]
   ): MkHListArbitrary[H :: T] =
    instance(
      Arbitrary {
        Gen.sized { size0 =>
          if (size0 < 0)
            // unlike positive values, don't split negative sizes any further, and let subsequent Gen handle them
            for {
              head <- Gen.resize(size0, Gen.lzy(headArbitrary.value.arbitrary))
              tail <- Gen.resize(size0, Gen.lzy(tailArbitrary.arbitrary.arbitrary))
            } yield head :: tail
          else {
            // take a fraction of approximately 1 / (n + 1) from size for the head, leave the
            // remaining for the tail

            val size = size0 max 0
            val remainder = size % (n() + 1)
            val fromRemainderGen =
              if (remainder > 0)
                Gen.choose(1, n()).map(r => if (r <= remainder) 1 else 0)
              else
                Gen.const(0)

            for {
              fromRemainder <- fromRemainderGen
              headSize = size / (n() + 1) + fromRemainder
              head <- Gen.resize(headSize, Gen.lzy(headArbitrary.value.arbitrary))
              tail <- Gen.resize(size - headSize, Gen.lzy(tailArbitrary.arbitrary.arbitrary))
            } yield head :: tail
          }
        }
      }
    )
}

trait MkRecursiveCoproductArbitrary[C <: Coproduct] {
  /** `Arbitrary[T]` instance built by this `MkRecursiveCoproductArbitrary[T]` */
  def arbitrary: Arbitrary[Recursive.Value[C]]
}

object MkRecursiveCoproductArbitrary {
  def apply[C <: Coproduct](implicit mkArb: MkRecursiveCoproductArbitrary[C]): MkRecursiveCoproductArbitrary[C] = mkArb

  def instance[C <: Coproduct](arb: => Arbitrary[Recursive.Value[C]]): MkRecursiveCoproductArbitrary[C] =
    new MkRecursiveCoproductArbitrary[C] {
      def arbitrary = arb
    }

  implicit val cnil: MkRecursiveCoproductArbitrary[CNil] =
    instance(Arbitrary(Gen.fail))

  implicit def ccons[H, T <: Coproduct, N <: Nat]
   (implicit
     headArbitrary: Strict[Arbitrary[H]],
     tailArbitrary: MkRecursiveCoproductArbitrary[T],
     length: ops.coproduct.Length.Aux[T, N],
     n: ops.nat.ToInt[N]
   ): MkRecursiveCoproductArbitrary[H :+: T] =
    instance(
      Arbitrary {
        Gen.sized {
          case n if n < 0 => Gen.const(Recursive.Value(None))
          case size =>
            val nextSize = size - 1
            Gen.frequency(
              1   -> Gen.resize(nextSize, Gen.lzy(headArbitrary.value.arbitrary)).map(h => Recursive.Value(Some(Inl(h)))),
              n() -> Gen.resize(nextSize, Gen.lzy(tailArbitrary.arbitrary.arbitrary)).map(_.map(Inr(_)))
            )
        }
      }
    )
}

trait MkCoproductArbitrary[C <: Coproduct] {
  /** `Arbitrary[T]` instance built by this `MkCoproductArbitrary[T]` */
  def arbitrary: Arbitrary[C]
}

object MkCoproductArbitrary {
  def apply[C <: Coproduct](implicit mkArb: MkCoproductArbitrary[C]): MkCoproductArbitrary[C] = mkArb

  def instance[C <: Coproduct](arb: => Arbitrary[C]): MkCoproductArbitrary[C] =
    new MkCoproductArbitrary[C] {
      def arbitrary = arb
    }

  implicit val cnil: MkCoproductArbitrary[CNil] =
    instance(Arbitrary(Gen.fail))

  implicit def ccons[H, T <: Coproduct, N <: Nat]
   (implicit
     headArbitrary: Strict[Arbitrary[H]],
     tailArbitrary: MkCoproductArbitrary[T],
     length: ops.coproduct.Length.Aux[T, N],
     n: ops.nat.ToInt[N]
   ): MkCoproductArbitrary[H :+: T] =
    instance(
      Arbitrary {
        Gen.sized { size =>
          /*
           * Unlike MkCoproductArbitrary above, try to generate a value no matter what (no Gen.fail).
           * This can blow the stack for recursive types, so should be avoided for those.
           */
          val nextSize = (size - 1) max 0
          Gen.frequency(
            1   -> Gen.resize(nextSize, Gen.lzy(headArbitrary.value.arbitrary)).map(Inl(_)),
            n() -> Gen.resize(nextSize, Gen.lzy(tailArbitrary.arbitrary.arbitrary)).map(Inr(_))
          )
        }
      }
    )
}
