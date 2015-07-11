package org.scalacheck
package derive

import shapeless._


trait MkShrink[T] {
  def shrink: Shrink[T]
}


trait MkDefaultShrink[T] extends MkShrink[T]

object MkDefaultShrink {
  def apply[T](implicit mkShrink: MkDefaultShrink[T]): MkDefaultShrink[T] = mkShrink

  def of[T](shrink0: => Shrink[T]): MkDefaultShrink[T] =
    new MkDefaultShrink[T] {
      def shrink = shrink0
    }


  implicit val hnilShrink: MkDefaultShrink[HNil] =
    of(Shrink.shrinkAny)
  implicit def hconsShrink[H, T <: HList]
   (implicit
     headShrink: Lazy[Shrink[H]],
     tailShrink: Lazy[MkDefaultShrink[T]]
   ): MkDefaultShrink[H :: T] =
    of(
      Shrink {
        case h :: t =>
          headShrink.value.shrink(h).map(_ :: t) #:::
            tailShrink.value.shrink.shrink(t).map(h :: _)
      }
    )

  implicit val cnilShrink: MkDefaultShrink[CNil] =
    of(Shrink.shrinkAny)
  implicit def cconsShrink[H, T <: Coproduct]
   (implicit
     headShrink: Lazy[Shrink[H]],
     tailShrink: Lazy[MkDefaultShrink[T]],
     headSingletons: Lazy[Singletons[H]],
     tailSingletons: Lazy[Singletons[T]]
   ): MkDefaultShrink[H :+: T] =
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
     shrink: Lazy[MkDefaultShrink[G]]
   ): MkDefaultShrink[F] =
    of(
      Shrink.xmap(gen.from, gen.to)(shrink.value.shrink)
    )

}
