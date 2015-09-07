package org.scalacheck

import shapeless._

import derive._
import util._

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

trait HListInstances {

  implicit def mkHListArbitrary[L <: HList]
   (implicit
     arb: MkHListArbitrary[L]
   ): Arbitrary[L] =
    arb.arbitrary

  implicit def mkHListCogen[L <: HList]
   (implicit
     arb: MkHListCogen[L]
   ): Cogen[L] =
    arb.cogen

  implicit def mkHListShrink[L <: HList]
   (implicit
     arb: MkHListShrink[L]
   ): Shrink[L] =
    arb.shrink

}

trait CoproductInstances {

  implicit def mkCoproductArbitrary[C <: Coproduct]
   (implicit
     arb: MkCoproductArbitrary[C]
   ): Arbitrary[C] =
    arb.arbitrary

  implicit def mkCoproductCogen[C <: Coproduct]
   (implicit
     arb: MkCoproductCogen[C]
   ): Cogen[C] =
    arb.cogen

  implicit def mkCoproductShrink[C <: Coproduct]
   (implicit
     arb: MkCoproductShrink[C]
   ): Shrink[C] =
    arb.shrink

}

trait DerivedInstances {

  implicit def mkArbitrary[T]
   (implicit
     priority: Strict.Global[LowPriority[
       Arbitrary[T],
       MkArbitrary[T]
     ]]
   ): Arbitrary[T] =
    priority.value.value.arbitrary

  implicit def mkShrink[T]
   (implicit
     priority: Strict.Global[LowPriority[
       Mask[Witness.`"Shrink.shrinkAny"`.T, Shrink[T]],
       MkShrink[T]
     ]]
   ): Shrink[T] =
    priority.value.value.shrink

  implicit def mkCogen[T]
   (implicit
     priority: Strict.Global[LowPriority[
       Cogen[T],
       MkCogen[T]
     ]]
   ): Cogen[T] =
    priority.value.value.cogen

}

object Shapeless
  extends SingletonInstances
  with HListInstances
  with CoproductInstances
  with DerivedInstances
