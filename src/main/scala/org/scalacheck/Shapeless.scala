package org.scalacheck

import shapeless._
import shapeless.ops.hlist.{ Length => HListLength }
import shapeless.ops.coproduct.{ Length => CoproductLength }
import shapeless.ops.nat.ToInt

object Shapeless {

  object ArbitraryDeriver {

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

  implicit def genericInstanceArbitrary[F, G](implicit
    gen: Generic.Aux[F, G],
    arbitrary: Lazy[Arbitrary[G]]
  ): Arbitrary[F] =
    Arbitrary(Gen.lzy(arbitrary.value.arbitrary).map(gen.from))

  } // ArbitraryDeriver

  implicit def instanceArbitrary[T](implicit orphan: Orphan[Arbitrary, ArbitraryDeriver.type, T]): Arbitrary[T] =
    orphan.instance


  object ShrinkDeriver {

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

  implicit def genericInstanceShrink[F, G](implicit
    gen: Generic.Aux[F, G],
    shrink: Lazy[Shrink[G]]
  ): Shrink[F] =
    Shrink.xmap(gen.from, gen.to)(shrink.value)

  } // ShrinkDeriver

  implicit def instanceShrink[T](implicit orphan: Orphan[Shrink, ShrinkDeriver.type, T]): Shrink[T] =
    orphan.instance


  /*
   * Forcing Option[T] to be viewed as a container, rather than a coproduct
   * For Shrink, the generated one is ok.
   *
   * It would be nice to generalize this kind of Arbitrary to any similar coproduct.
   */
  implicit def keepDefaultOptionArbitrary[T: Arbitrary]: Arbitrary[Option[T]] = Arbitrary.arbOption[T]

  /*
   * Forcing List[T] to be viewed as a container, rather than a coproduct
   */
  implicit def keepDefaultListArbitrary[T: Arbitrary]: Arbitrary[List[T]] = Arbitrary.arbContainer[List, T]
  implicit def keepDefaultListShrink[T: Shrink]: Shrink[List[T]] = Shrink.shrinkContainer[List, T]
  
}
