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
 *     val shrink: Shrink[T] = MkShrink[T].shrink
 * or look up for an implicit `MkShrink[T]`.
 */
trait MkShrink[T] {
  /** `Shrink[T]` instance built by this `MkShrink[T]` */
  def shrink: Shrink[T]
}

object MkShrink {
  def apply[T](implicit mkShrink: MkShrink[T]): MkShrink[T] = mkShrink

  def instance[T](shrink0: => Shrink[T]): MkShrink[T] =
    new MkShrink[T] {
      def shrink = shrink0
    }

  private def lazyxmap[T, U](from: T => U, to: U => T)(st: => Shrink[T]): Shrink[U] = Shrink[U] { u: U ⇒
    st.shrink(to(u)).map(from)
  }

  implicit def genericProduct[P, L <: HList]
   (implicit
     gen: Generic.Aux[P, L],
     shrink: Lazy[MkHListShrink[L]]
   ): MkShrink[P] =
    instance(
      lazyxmap(gen.from, gen.to)(shrink.value.shrink)
    )

  @deprecated("Kept for binary compatibility", "1.1.7")
  def genericCoproduct[S, C <: Coproduct]
   (implicit
     gen: Generic.Aux[S, C],
     shrink: Lazy[MkCoproductShrink[C]]
   ): MkShrink[S] =
    instance(
      lazyxmap(gen.from, gen.to)(shrink.value.shrink)
    )

  implicit def genericCoproduct0[S, C <: Coproduct]
   (implicit
     gen: Generic.Aux[S, C],
     shrink: Lazy[MkCoproductShrink0[C]]
   ): MkShrink[S] =
    instance(
      lazyxmap(gen.from, gen.to)(shrink.value.shrink)
    )
}

trait MkHListShrink[L <: HList] {
  /** `Shrink[T]` instance built by this `MkHListShrink[T]` */
  def shrink: Shrink[L]
}

object MkHListShrink {
  def apply[L <: HList](implicit mkShrink: MkHListShrink[L]): MkHListShrink[L] = mkShrink

  def instance[L <: HList](shrink0: => Shrink[L]): MkHListShrink[L] =
    new MkHListShrink[L] {
      def shrink = shrink0
    }

  implicit val hnil: MkHListShrink[HNil] =
    instance(Shrink.shrinkAny)

  implicit def hcons[H, T <: HList]
   (implicit
     headShrink: Strict[Shrink[H]],
     tailShrink: MkHListShrink[T]
   ): MkHListShrink[H :: T] =
    instance(
      Shrink {
        case h :: t =>
          headShrink.value.shrink(h).map(_ :: t) #:::
            tailShrink.shrink.shrink(t).map(h :: _)
      }
    )
}

@deprecated("See MkCoproductShrink0 instead, which has no quadratic implicit lookups", "1.1.7")
trait MkCoproductShrink[C <: Coproduct] {
  /** `Shrink[T]` instance built by this `MkCoproductShrink[T]` */
  def shrink: Shrink[C]
}

object MkCoproductShrink {
  def apply[T <: Coproduct](implicit mkShrink: MkCoproductShrink[T]): MkCoproductShrink[T] = mkShrink

  def instance[T <: Coproduct](shrink0: => Shrink[T]): MkCoproductShrink[T] =
    new MkCoproductShrink[T] {
      def shrink = shrink0
    }

  implicit val cnil: MkCoproductShrink[CNil] =
    instance(Shrink.shrinkAny)

  implicit def ccons[H, T <: Coproduct]
   (implicit
     headShrink: Strict[Shrink[H]],
     tailShrink: MkCoproductShrink[T],
     headSingletons: Strict[Singletons[H]],
     tailSingletons: Strict[Singletons[T]]
   ): MkCoproductShrink[H :+: T] =
    instance(
      Shrink {
        case Inl(h) =>
          if (headSingletons.value().contains(h)) Stream.empty
          else tailSingletons.value().toStream.map(Inr(_)) ++ headShrink.value.shrink(h).map(Inl(_))
        case Inr(t) =>
          if (tailSingletons.value().contains(t)) Stream.empty
          else headSingletons.value().toStream.map(Inl(_)) ++ tailShrink.shrink.shrink(t).map(Inr(_))
      }
    )
}

abstract class MkCoproductShrink0[C <: Coproduct] {
  /** `Shrink[T]` instance built by this `MkCoproductShrink0[T]` */
  final def shrink: Shrink[C] =
    Shrink(apply(_).getOrElse(Stream.empty))

  /**
    * Shrink a value.
    *
    * @return A [[scala.Stream]] of shrunk values, wrapped in [[scala.Some]], or [[scala.None]] if the value cannot be shrunk any more.
    */
  def apply(c: C): Option[Stream[C]]
  def singletons: Singletons[C]
}

object MkCoproductShrink0 {
  def apply[T <: Coproduct](implicit mkShrink: MkCoproductShrink0[T]): MkCoproductShrink0[T] = mkShrink

  def instance[T <: Coproduct](singletons0: Singletons[T])(shrink0: T => Option[Stream[T]]): MkCoproductShrink0[T] =
    new MkCoproductShrink0[T] {
      def apply(t: T) = shrink0(t)
      def singletons = singletons0
    }

  implicit val cnil: MkCoproductShrink0[CNil] =
    instance(Singletons.empty)(_ => Some(Stream.empty))

  implicit def ccons[H, T <: Coproduct]
   (implicit
     headShrink: Strict[Shrink[H]],
     tailShrink: MkCoproductShrink0[T],
     headSingletons: Strict[Singletons[H]]
   ): MkCoproductShrink0[H :+: T] = {

    val singletons = Singletons.instance(headSingletons.value().map(Inl(_): H :+: T) ++ tailShrink.singletons().map(Inr(_): H :+: T))
    lazy val headSingletonsSet = headSingletons.value().toSet

    instance[H :+: T](singletons) {
      case Inl(h) =>
        if (headSingletonsSet(h))
          None
        else
          Some(tailShrink.singletons().toStream.map(Inr(_)) ++ headShrink.value.shrink(h).map(Inl(_)))
      case Inr(t) =>
        tailShrink(t).map(_.map(Inr(_)) ++ headSingletons.value().toStream.map(Inl(_)))
    }
  }
}
