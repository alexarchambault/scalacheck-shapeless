package org.scalacheck

import shapeless._
import shapeless.ops.hlist.{ Length => HListLength }
import shapeless.ops.coproduct.{ Length => CoproductLength }
import shapeless.ops.nat.ToInt

object Shapeless {

  implicit val hnilArbitrary: Arbitrary[HNil] =
    Arbitrary(Gen.const(HNil))
  
  implicit def hconsArbitrary[H, T <: HList, N <: Nat](implicit
    headArbitrary: Lazy[Arbitrary[H]],
    tailArbitrary: Lazy[Arbitrary[T]],
    length: HListLength.Aux[T, N],
    n: ToInt[N]
  ): Arbitrary[H :: T] =
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
          tail <- Gen.resize(size - headSize, Gen.lzy(tailArbitrary.value.arbitrary))
        } yield head :: tail
      }
    }
  
  implicit val cnilArbitrary: Arbitrary[CNil] = Arbitrary(Gen.fail)

  implicit def cconsEquallyWeightedArbitrary[H, T <: Coproduct, N <: Nat](implicit
    headArbitrary: Lazy[Arbitrary[H]],
    tailArbitrary: Lazy[Arbitrary[T]],
    length: CoproductLength.Aux[T, N],
    n: ToInt[N]
  ): Arbitrary[H :+: T] =
    Arbitrary {
      Gen.sized {
        case 0 => Gen.fail
        case size =>
          val sig = math.signum(size)

          Gen.frequency(
            1   -> Gen.resize(size - sig, Gen.lzy(headArbitrary.value.arbitrary)).map(Inl(_)),
            n() -> Gen.resize(size - sig, Gen.lzy(tailArbitrary.value.arbitrary)).map(Inr(_))
          )
      }
    }

  implicit def instanceArbitrary[F, G](implicit
    gen: Generic.Aux[F, G],
    arbitrary: Lazy[Arbitrary[G]]
  ): Arbitrary[F] =
    Arbitrary(Gen.lzy(arbitrary.value.arbitrary).map(gen.from))


  /*
   * No need for specific shrinks for HNil/CNil
   */

  implicit def hconsShrink[H, T <: HList](implicit
    headShrink: Lazy[Shrink[H]],
    tailShrink: Lazy[Shrink[T]]
  ): Shrink[H :: T] =
    Shrink {
      case h :: t => headShrink.value.shrink(h).map(_ :: t) #::: tailShrink.value.shrink(t).map(h :: _)
    }

  implicit def cconsShrink[H, T <: Coproduct](implicit
    headShrink: Lazy[Shrink[H]],
    tailShrink: Lazy[Shrink[T]],
    headSingletons: Lazy[Singletons[H]],
    tailSingletons: Lazy[Singletons[T]]
  ): Shrink[H :+: T] =
    Shrink {
      case Inl(h) =>
        tailSingletons.value().toStream.map(Inr(_)) ++ headShrink.value.shrink(h).map(Inl(_))
      case Inr(t) =>
        headSingletons.value().toStream.map(Inl(_)) ++ tailShrink.value.shrink(t).map(Inr(_))
    }

  implicit def instanceShrink[F, G](implicit
    gen: Generic.Aux[F, G],
    shrink: Lazy[Shrink[G]]
  ): Shrink[F] =
    Shrink.xmap(gen.from, gen.to)(shrink.value)

  /*
   * Forcing List[T] to be shrinked as a container, rather than a coproduct
   */
  implicit def keepDefaultListShrink[T: Shrink]: Shrink[List[T]] = Shrink.shrinkContainer[List, T]
  
}
