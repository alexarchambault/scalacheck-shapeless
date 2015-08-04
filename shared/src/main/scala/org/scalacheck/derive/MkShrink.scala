package org.scalacheck
package derive

import shapeless._

/**
 * Derives `Shrink[T]` instances for `T` an `HList`, a `Coproduct`,
 * a case class or an ADT (or more generally, a type represented
 * `Generic`ally as an `HList` or a `Coproduct`).
 *
 * The instances derived here are more specific than the default ones
 * derived for any type by `Shrink.shrinkAny`.
 *
 * Use like
 *     val arbitrary: Arbitrary[T] = MkArbitrary[T].arbitrary
 * or look up for an implicit `MkArbitrary[T]`.
 */
trait MkShrink[T] {
  /** `Shrink[T]` instance built by this `MkShrink[T]` */
  def shrink: Shrink[T]
}

object MkShrink {
  def apply[T](implicit mkShrink: MkShrink[T]): MkShrink[T] = mkShrink

  def of[T](shrink0: => Shrink[T]): MkShrink[T] =
    new MkShrink[T] {
      def shrink = shrink0
    }

  implicit val hnilShrink: MkShrink[HNil] =
    of(Shrink.shrinkAny)
  implicit def hconsShrink[H, T <: HList]
   (implicit
     headShrink: Lazy[Shrink[H]],
     tailShrink: Lazy[MkShrink[T]]
   ): MkShrink[H :: T] =
    of(
      Shrink {
        case h :: t =>
          headShrink.value.shrink(h).map(_ :: t) #:::
            tailShrink.value.shrink.shrink(t).map(h :: _)
      }
    )

  implicit val cnilShrink: MkShrink[CNil] =
    of(Shrink.shrinkAny)
  implicit def cconsShrink[H, T <: Coproduct]
   (implicit
     headShrink: Lazy[Shrink[H]],
     tailShrink: Lazy[MkShrink[T]],
     headSingletons: Lazy[Singletons[H]],
     tailSingletons: Lazy[Singletons[T]]
   ): MkShrink[H :+: T] =
    of(
      Shrink {
        case Inl(h) =>
          tailSingletons.value().toStream.map(Inr(_)) ++ headShrink.value.shrink(h).map(Inl(_))
        case Inr(t) =>
          headSingletons.value().toStream.map(Inl(_)) ++ tailShrink.value.shrink.shrink(t).map(Inr(_))
      }
    )

  implicit def genericShrink[F, G]
   (implicit
     gen: Generic.Aux[F, G],
     shrink: Lazy[MkShrink[G]]
   ): MkShrink[F] =
    of(
      Shrink.xmap(gen.from, gen.to)(shrink.value.shrink)
    )

}
