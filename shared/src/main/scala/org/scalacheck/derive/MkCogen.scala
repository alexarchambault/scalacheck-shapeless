package org.scalacheck
package derive

import org.scalacheck.rng.Seed
import shapeless.compat._

import shapeless.{ Lazy => _, _ }

/**
 * Derives `Cogen[T]` instances for `T` an `HList`, a `Coproduct`,
 * a case class or an ADT (or more generally, a type represented
 * `Generic`ally as an `HList` or a `Coproduct`).
 *
 * Use like
 *     val cogen: Cogen[T] = MkCogen[T].cogen
 * or look up for an implicit `MkCogen[T]`.
 */
trait MkCogen[T] {
  /** `Cogen[T]` instance built by this `MkCogen[T]` */
  def cogen: Cogen[T]
}

trait MkHListCogen[L <: HList] {
  /** `Cogen[T]` instance built by this `MkCogen[T]` */
  def cogen: Cogen[L]
}

trait MkCoproductCogen[C <: Coproduct] {
  /** `Cogen[T]` instance built by this `MkCogen[T]` */
  def cogen: Cogen[C]
}

object MkHListCogen {
  def apply[L <: HList](implicit mkCogen: MkHListCogen[L]): MkHListCogen[L] = mkCogen

  def of[L <: HList](cogen0: => Cogen[L]): MkHListCogen[L] =
    new MkHListCogen[L] {
      def cogen = cogen0
    }

  implicit lazy val hnilCogen: MkHListCogen[HNil] =
    of(Cogen.cogenUnit.contramap(_ => ()))

  implicit def hconsCogen[H, T <: HList]
   (implicit
     headCogen: Strict[Cogen[H]],
     tailCogen: MkHListCogen[T]
   ): MkHListCogen[H :: T] =
    of(
      Cogen({case (seed, h :: t) =>
        tailCogen.cogen.perturb(headCogen.value.perturb(seed, h), t)
      }: (Seed, H :: T) => Seed)
    )
}

object MkCoproductCogen {
  def apply[C <: Coproduct](implicit mkCogen: MkCoproductCogen[C]): MkCoproductCogen[C] = mkCogen

  def of[C <: Coproduct](cogen0: => Cogen[C]): MkCoproductCogen[C] =
    new MkCoproductCogen[C] {
      def cogen = cogen0
    }

  implicit lazy val cnilCogen: MkCoproductCogen[CNil] =
    of(Cogen.cogenUnit.contramap(_ => ()))

  implicit def cconsCogen[H, T <: Coproduct]
   (implicit
     headCogen: Strict[Cogen[H]],
     tailCogen: MkCoproductCogen[T]
   ): MkCoproductCogen[H :+: T] =
    of(
      Cogen({
        case (seed, Inl(h)) =>
          headCogen.value.perturb(seed, h)
        case (seed, Inr(t)) =>
          tailCogen.cogen.perturb(seed.next, t)
      }: (Seed, H :+: T) => Seed)
    )
}

object MkCogen {
  def apply[T](implicit mkCogen: MkCogen[T]): MkCogen[T] = mkCogen

  def of[T](cogen0: => Cogen[T]): MkCogen[T] =
    new MkCogen[T] {
      def cogen = cogen0
    }

  implicit def genericProductCogen[P, L <: HList]
   (implicit
     gen: Generic.Aux[P, L],
     cogen: Lazy[MkHListCogen[L]]
   ): MkCogen[P] =
    of(cogen.value.cogen.contramap(gen.to))

  implicit def genericCoproductCogen[S, C <: Coproduct]
   (implicit
     gen: Generic.Aux[S, C],
     cogen: Lazy[MkCoproductCogen[C]]
   ): MkCogen[S] =
    of(cogen.value.cogen.contramap(gen.to))
}
