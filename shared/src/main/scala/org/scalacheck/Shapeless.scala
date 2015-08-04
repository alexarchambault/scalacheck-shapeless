package org.scalacheck

import shapeless._

import derive._

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

trait DerivedInstances {

  implicit def mkArbitrary[T]
   (implicit
     priority: Strict.Cached[LowPriority[
       Arbitrary[T],
       MkDefaultArbitrary[T]
     ]]
   ): Arbitrary[T] =
    priority.value.value.arbitrary

  implicit def mkShrink[T]
   (implicit
     priority: Strict.Cached[LowPriority[
       Shrink[T],
       MkDefaultShrink[T]
     ]]
   ): Shrink[T] =
    priority.value.value.shrink

  implicit def mkCogen[T]
   (implicit
     priority: Strict.Cached[LowPriority[
       Cogen[T],
       MkDefaultCogen[T]
     ]]
   ): Cogen[T] =
    priority.value.value.cogen

}

object Shapeless extends SingletonInstances with DerivedInstances