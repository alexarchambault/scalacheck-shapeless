package org.scalacheck
package derive

import shapeless.{ Lazy => _, _ }
import shapeless.compat._


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

object MkArbitrary {
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

  implicit def genericCoproduct[S, C <: Coproduct]
   (implicit
     gen: Generic.Aux[S, C],
     mkArb: Lazy[MkCoproductArbitrary[C]]
   ): MkArbitrary[S] =
    instance(
      Arbitrary(Gen.lzy(mkArb.value.arbitrary.arbitrary).map(gen.from))
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
        Gen.sized { size =>
          val sig = math.signum(size)
          val remainder = sig * size % (n() + 1)
          val fromRemainderGen =
            if (remainder > 0)
              Gen.choose(1, n()).map(r => if (r <= remainder) sig else 0)
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
    )
}

trait MkCoproductArbitrary[C <: Coproduct] {
  /** `Arbitrary[T]` instance built by this `MkArbitraryCoproduct[T]` */
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
        Gen.sized {
          case 0 => Gen.fail
          case size =>
            val sig = math.signum(size)

            Gen.frequency(
              1   -> Gen.resize(size - sig, Gen.lzy(headArbitrary.value.arbitrary)).map(Inl(_)),
              n() -> Gen.resize(size - sig, Gen.lzy(tailArbitrary.arbitrary.arbitrary)).map(Inr(_))
            )
        }
      }
    )
}
