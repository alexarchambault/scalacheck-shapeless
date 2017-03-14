package org.scalacheck.derive

import org.scalacheck.{Arbitrary, Cogen, Gen, Shrink}
import shapeless.{Coproduct, HList, LowPriority, Strict, Witness}
import shapeless.labelled._

trait SingletonInstances {

  implicit def arbitrarySingletonType[S]
   (implicit
     w: Witness.Aux[S]
   ): Arbitrary[S] =
    Arbitrary(Gen.const(w.value))

  /**
    * Derives `Cogen[T]` instances for `T` a singleton type, like
    * `Witness.``"str"``.T` or `Witness.``true``.T` for example.
    *
    * The generated `Cogen[T]` behaves like `Cogen[Unit]`, as like
    * `Unit`, singleton types only have one instance.
    */
  implicit def cogenSingletonType[S]
   (implicit
     w: Witness.Aux[S]
   ): Cogen[S] =
    Cogen.cogenUnit
      // Extra contramap, that inserts a `next` call on the returned seeds,
      // so that case objects are returned the same Cogen here and when derived through Generic.
      .contramap[Unit](identity)
      .contramap[S](_ => ())
}

trait FieldTypeInstances {

  implicit def arbitraryFieldType[K, H]
   (implicit
     underlying: Arbitrary[H]
   ): Arbitrary[FieldType[K, H]] =
    Arbitrary(
      underlying
        .arbitrary
        .map(field[K](_))
    )

  implicit def cogenFieldType[K, H]
   (implicit
     underlying: Cogen[H]
   ): Cogen[FieldType[K, H]] =
    underlying
      .contramap(h => h: H)

  implicit def shrinkFieldType[K, H]
   (implicit
     underlying: Shrink[H]
   ): Shrink[FieldType[K, H]] =
    Shrink.xmap[H, FieldType[K, H]](field[K](_), h => h: H)(underlying)
}

trait HListInstances {

  implicit def hlistArbitrary[L <: HList]
   (implicit
     arb: MkHListArbitrary[L]
   ): Arbitrary[L] =
    arb.arbitrary

  implicit def hlistCogen[L <: HList]
   (implicit
     arb: MkHListCogen[L]
   ): Cogen[L] =
    arb.cogen

  implicit def hlistShrink[L <: HList]
   (implicit
     arb: MkHListShrink[L]
   ): Shrink[L] =
    arb.shrink
}

trait CoproductInstances {

  implicit def coproductArbitrary[C <: Coproduct]
   (implicit
     arb: MkCoproductArbitrary[C]
   ): Arbitrary[C] =
    arb.arbitrary

  implicit def coproductCogen[C <: Coproduct]
   (implicit
     arb: MkCoproductCogen[C]
   ): Cogen[C] =
    arb.cogen

  implicit def coproductShrink[C <: Coproduct]
   (implicit
     arb: MkCoproductShrink[C]
   ): Shrink[C] =
    arb.shrink
}

trait DerivedInstances {

  implicit def derivedArbitrary[T]
   (implicit
     ev: LowPriority,
     underlying: Strict[MkArbitrary[T]]
   ): Arbitrary[T] =
    underlying.value.arbitrary

  implicit def derivedShrink[T]
   (implicit
     ev: LowPriority.Ignoring[Witness.`"shrinkAny"`.T],
     underlying: Strict[MkShrink[T]]
   ): Shrink[T] =
    underlying.value.shrink

  implicit def derivedCogen[T]
   (implicit
     ev: LowPriority,
     underlying: Strict[MkCogen[T]]
   ): Cogen[T] =
    underlying.value.cogen
}

trait EnumerationInstances {

  implicit def arbitraryEnumerationValue[E <: Enumeration]
   (implicit
     w: Witness.Aux[E]
   ): Arbitrary[E#Value] =
    Arbitrary(Gen.oneOf(w.value.values.toSeq))
}
