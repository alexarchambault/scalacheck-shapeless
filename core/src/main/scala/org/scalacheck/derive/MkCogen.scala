package org.scalacheck
package derive

import org.scalacheck.rng.Seed
import shapeless._


trait MkCogen[T] {
  def cogen: Cogen[T]
}


trait MkDefaultCogen[T] extends MkCogen[T]

object MkDefaultCogen {
  def apply[T](implicit mkCogen: MkDefaultCogen[T]): MkDefaultCogen[T] = mkCogen

  def of[T](cogen0: => Cogen[T]): MkDefaultCogen[T] =
    new MkDefaultCogen[T] {
      def cogen = cogen0
    }

  implicit lazy val hnilCogen: MkDefaultCogen[HNil] =
    of(Cogen.cogenUnit.contramap(_ => ()))

  implicit def hconsCogen[H, T <: HList]
   (implicit
     headCogen: Lazy[Cogen[H]],
     tailCogen: Lazy[MkDefaultCogen[T]]
   ): MkDefaultCogen[H :: T] =
    of(
      Cogen({case (seed, h :: t) =>
        tailCogen.value.cogen.perturb(headCogen.value.perturb(seed, h), t)
      }: (Seed, H :: T) => Seed)
    )

  implicit lazy val cnilCogen: MkDefaultCogen[CNil] =
    of(Cogen.cogenUnit.contramap(_ => ()))

  implicit def cconsCogen[H, T <: Coproduct]
   (implicit
     headCogen: Lazy[Cogen[H]],
     tailCogen: Lazy[MkDefaultCogen[T]]
   ): MkDefaultCogen[H :+: T] =
    of(
      Cogen({
        case (seed, Inl(h)) =>
          headCogen.value.perturb(seed, h)
        case (seed, Inr(t)) =>
          tailCogen.value.cogen.perturb(seed.next, t)
      }: (Seed, H :+: T) => Seed)
    )

  implicit def instanceCogen[F, G]
   (implicit
     gen: Generic.Aux[F, G],
     cogen: Lazy[MkDefaultCogen[G]]
   ): MkDefaultCogen[F] =
    of(cogen.value.cogen.contramap(gen.to))
}


trait MkSingletonCogen[T] extends MkCogen[T]

object MkSingletonCogen {
  def apply[T](implicit mkCogen: MkSingletonCogen[T]): MkSingletonCogen[T] = mkCogen

  def of[T](cogen0: => Cogen[T]): MkSingletonCogen[T] =
    new MkSingletonCogen[T] {
      def cogen = cogen0
    }

  implicit def singletonCogen[S]
   (implicit
     w: Witness.Aux[S]
   ): MkSingletonCogen[S] =
    of(Cogen.cogenUnit.contramap(_ => ()))
}