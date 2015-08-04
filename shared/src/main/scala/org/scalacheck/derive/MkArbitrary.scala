package org.scalacheck
package derive

import shapeless._


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


trait MkHListArbitrary[L <: HList] {
  /** `Arbitrary[T]` instance built by this `MkArbitraryHList[T]` */
  def arbitrary: Arbitrary[L]
}

object MkHListArbitrary {
  def apply[L <: HList](implicit mkArb: MkHListArbitrary[L]): MkHListArbitrary[L] = mkArb

  def of[L <: HList](arb: => Arbitrary[L]): MkHListArbitrary[L] =
    new MkHListArbitrary[L] {
      def arbitrary = arb
    }

  implicit val hnilMkArb: MkHListArbitrary[HNil] =
    of(Arbitrary(Gen.const(HNil)))

  implicit def hconsMkArb[H, T <: HList, N <: Nat]
   (implicit
     headArbitrary: Lazy[Arbitrary[H]],
     tailArbitrary: Lazy[MkHListArbitrary[T]],
     length: ops.hlist.Length.Aux[T, N],
     n: ops.nat.ToInt[N]
   ): MkHListArbitrary[H :: T] =
    of(
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
            tail <- Gen.resize(size - headSize, Gen.lzy(tailArbitrary.value.arbitrary.arbitrary))
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

  def of[C <: Coproduct](arb: => Arbitrary[C]): MkCoproductArbitrary[C] =
    new MkCoproductArbitrary[C] {
      def arbitrary = arb
    }

  implicit val cnilMkArb: MkCoproductArbitrary[CNil] =
    of(Arbitrary(Gen.fail))

  implicit def cconsMkArb[H, T <: Coproduct, N <: Nat]
   (implicit
     headArbitrary: Lazy[Arbitrary[H]],
     tailArbitrary: Lazy[MkCoproductArbitrary[T]],
     length: ops.coproduct.Length.Aux[T, N],
     n: ops.nat.ToInt[N]
   ): MkCoproductArbitrary[H :+: T] =
    of(
      Arbitrary {
        Gen.sized {
          case 0 => Gen.fail
          case size =>
            val sig = math.signum(size)

            Gen.frequency(
              1   -> Gen.resize(size - sig, Gen.lzy(headArbitrary.value.arbitrary)).map(Inl(_)),
              n() -> Gen.resize(size - sig, Gen.lzy(tailArbitrary.value.arbitrary.arbitrary)).map(Inr(_))
            )
        }
      }
    )
}


object MkArbitrary {
  def apply[T](implicit mkArb: MkArbitrary[T]): MkArbitrary[T] = mkArb

  def of[T](arb: => Arbitrary[T]): MkArbitrary[T] =
    new MkArbitrary[T] {
      def arbitrary = arb
    }

  implicit def genericProductMkArb[P, L <: HList]
   (implicit
     gen: Generic.Aux[P, L],
     mkArb: Lazy[MkHListArbitrary[L]]
   ): MkArbitrary[P] =
    of(
      Arbitrary(Gen.lzy(mkArb.value.arbitrary.arbitrary).map(gen.from))
    )

  implicit def genericCoproductMkArb[S, C <: Coproduct]
   (implicit
     gen: Generic.Aux[S, C],
     mkArb: Lazy[MkCoproductArbitrary[C]]
   ): MkArbitrary[S] =
    of(
      Arbitrary(Gen.lzy(mkArb.value.arbitrary.arbitrary).map(gen.from))
    )
}
