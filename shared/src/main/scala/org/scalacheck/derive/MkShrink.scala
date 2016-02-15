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

trait MkHListShrink[L <: HList] {
  /** `Shrink[T]` instance built by this `MkHListShrink[T]` */
  def shrink: Shrink[L]
}

trait MkCoproductShrink[C <: Coproduct] {
  /** `Shrink[T]` instance built by this `MkCoproductShrink[T]` */
  def shrink: Shrink[C]
}

object MkHListShrink {
  def apply[T <: HList](implicit mkShrink: MkHListShrink[T]): MkHListShrink[T] = mkShrink

  def of[T <: HList](shrink0: => Shrink[T]): MkHListShrink[T] =
    new MkHListShrink[T] {
      def shrink = shrink0
    }

  implicit val hnilShrink: MkHListShrink[HNil] =
    of(Shrink.shrinkAny)

  implicit def hconsShrink[H, T <: HList]
   (implicit
     headShrink: Strict[Shrink[H]],
     tailShrink: MkHListShrink[T]
   ): MkHListShrink[H :: T] =
    of(
      Shrink {
        case h :: t =>
          headShrink.value.shrink(h).map(_ :: t) #:::
            tailShrink.shrink.shrink(t).map(h :: _)
      }
    )
}

object MkCoproductShrink {
  def apply[T <: Coproduct](implicit mkShrink: MkCoproductShrink[T]): MkCoproductShrink[T] = mkShrink

  def of[T <: Coproduct](shrink0: => Shrink[T]): MkCoproductShrink[T] =
    new MkCoproductShrink[T] {
      def shrink = shrink0
    }

  implicit val cnilShrink: MkCoproductShrink[CNil] =
    of(Shrink.shrinkAny)

  implicit def cconsShrink[H, T <: Coproduct]
   (implicit
     headShrink: Strict[Shrink[H]],
     tailShrink: MkCoproductShrink[T],
     headSingletons: Strict[Singletons[H]],
     tailSingletons: Strict[Singletons[T]]
   ): MkCoproductShrink[H :+: T] =
    of(
      Shrink {
        case Inl(h) =>
          tailSingletons.value().toStream.map(Inr(_)) ++ headShrink.value.shrink(h).map(Inl(_))
        case Inr(t) =>
          headSingletons.value().toStream.map(Inl(_)) ++ tailShrink.shrink.shrink(t).map(Inr(_))
      }
    )
}

object MkShrink {
  def apply[T](implicit mkShrink: MkShrink[T]): MkShrink[T] = mkShrink

  def of[T](shrink0: => Shrink[T]): MkShrink[T] =
    new MkShrink[T] {
      def shrink = shrink0
    }

  private def lazyxmap[T, U](from: T => U, to: U => T)(st: => Shrink[T]): Shrink[U] = Shrink[U] { u: U ⇒
    st.shrink(to(u)).map(from)
  }

  implicit def genericProductShrink[P, L <: HList]
   (implicit
     gen: Generic.Aux[P, L],
     shrink: Lazy[MkHListShrink[L]]
   ): MkShrink[P] =
    of(
      lazyxmap(gen.from, gen.to)(shrink.value.shrink)
    )

  implicit def genericCoproductShrink[S, C <: Coproduct]
   (implicit
     gen: Generic.Aux[S, C],
     shrink: Lazy[MkCoproductShrink[C]]
   ): MkShrink[S] =
    of(
      lazyxmap(gen.from, gen.to)(shrink.value.shrink)
    )

}
