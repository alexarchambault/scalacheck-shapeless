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

object MkArbitrary {
  def apply[T](implicit mkArb: MkArbitrary[T]): MkArbitrary[T] = mkArb

  def of[T](arb: => Arbitrary[T]): MkArbitrary[T] =
    new MkArbitrary[T] {
      def arbitrary = arb
    }

  implicit val hnilMkArb: MkArbitrary[HNil] =
    of(Arbitrary(Gen.const(HNil)))
  implicit def hconsMkArb[H, T <: HList, N <: Nat]
   (implicit
     headArbitrary: Lazy[Arbitrary[H]],
     tailArbitrary: Lazy[MkArbitrary[T]],
     length: ops.hlist.Length.Aux[T, N],
     n: ops.nat.ToInt[N]
   ): MkArbitrary[H :: T] =
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

  implicit val cnilMkArb: MkArbitrary[CNil] =
    of(Arbitrary(Gen.fail))
  implicit def cconsMkArb[H, T <: Coproduct, N <: Nat]
   (implicit
     headArbitrary: Lazy[Arbitrary[H]],
     tailArbitrary: Lazy[MkArbitrary[T]],
     length: ops.coproduct.Length.Aux[T, N],
     n: ops.nat.ToInt[N]
   ): MkArbitrary[H :+: T] =
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

  implicit def genericMkArb[F, G]
   (implicit
     gen: Generic.Aux[F, G],
     mkArb: Lazy[MkArbitrary[G]]
   ): MkArbitrary[F] =
    of(
      Arbitrary(Gen.lzy(mkArb.value.arbitrary.arbitrary).map(gen.from))
    )
}
