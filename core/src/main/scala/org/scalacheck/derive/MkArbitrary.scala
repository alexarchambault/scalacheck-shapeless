package org.scalacheck
package derive

import shapeless._

/** Base trait of `Arbitrary[T]` generating type classes. */
trait MkArbitrary[T] {
  /** `Arbitrary[T]` instance built by this `MkArbitrary[T]` */
  def arbitrary: Arbitrary[T]
}

/**
 * Derives `Arbitrary[T]` instances for `T` an `HList`, a `Coproduct`,
 * a case class or an ADT (or more generally, a type represented
 * `Generic`ally as an `HList` or a `Coproduct`).
 *
 * Use like
 *     val arbitrary: Arbitrary[T] = MkDefaultArbitrary[T].arbitrary
 * or look up for an implicit `MkDefaultArbitrary[T]`.
 */
trait MkDefaultArbitrary[T] extends MkArbitrary[T]

object MkDefaultArbitrary {
  def apply[T](implicit mkArb: MkDefaultArbitrary[T]): MkDefaultArbitrary[T] = mkArb

  def of[T](arb: => Arbitrary[T]): MkDefaultArbitrary[T] =
    new MkDefaultArbitrary[T] {
      def arbitrary = arb
    }

  implicit val hnilMkArb: MkDefaultArbitrary[HNil] =
    of(Arbitrary(Gen.const(HNil)))
  implicit def hconsMkArb[H, T <: HList, N <: Nat]
   (implicit
     headArbitrary: Lazy[Arbitrary[H]],
     tailArbitrary: Lazy[MkDefaultArbitrary[T]],
     length: ops.hlist.Length.Aux[T, N],
     n: ops.nat.ToInt[N]
   ): MkDefaultArbitrary[H :: T] =
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

  implicit val cnilMkArb: MkDefaultArbitrary[CNil] =
    of(Arbitrary(Gen.fail))
  implicit def cconsMkArb[H, T <: Coproduct, N <: Nat]
   (implicit
     headArbitrary: Lazy[Arbitrary[H]],
     tailArbitrary: Lazy[MkDefaultArbitrary[T]],
     length: ops.coproduct.Length.Aux[T, N],
     n: ops.nat.ToInt[N]
   ): MkDefaultArbitrary[H :+: T] =
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
     mkArb: Lazy[MkDefaultArbitrary[G]]
   ): MkDefaultArbitrary[F] =
    of(
      Arbitrary(Gen.lzy(mkArb.value.arbitrary.arbitrary).map(gen.from))
    )
}


trait MkSingletonArbitrary[T] extends MkArbitrary[T]

object MkSingletonArbitrary {
  def apply[T](implicit mkArb: MkSingletonArbitrary[T]): MkSingletonArbitrary[T] = mkArb

  def of[T](arb: => Arbitrary[T]): MkSingletonArbitrary[T] =
    new MkSingletonArbitrary[T] {
      def arbitrary = arb
    }

  implicit def singletonArb[S]
   (implicit
     w: Witness.Aux[S]
   ): MkSingletonArbitrary[S] =
    of(Arbitrary(Gen.const(w.value)))
}
