package org.scalacheck

import shapeless.{ Lazy => _, _ }
import shapeless.compat._
import shapeless.labelled._

/**
 * Type class providing the instances of `T` that can be built out of
 * singletons only.
 *
 * Used by the derived Shrink instances for ADTs in particular.
 */
trait Singletons[T] {
  /**
   * Instances of `T` that can be built out of singletons, or
   * an empty sequence if none were found.
   */
  def apply(): Seq[T]
}

trait HListSingletons[L <: HList] {
  /**
   * Instances of `L` that can be built out of singletons, or
   * an empty sequence if none were found.
   */
  def apply(): Seq[L]
}

trait CoproductSingletons[C <: Coproduct] {
  /**
   * Instances of `C` that can be built out of singletons, or
   * an empty sequence if none were found.
   */
  def apply(): Seq[C]
}

object HListSingletons {
  def apply[L <: HList](implicit s: HListSingletons[L]): HListSingletons[L] = s

  def singletons[L <: HList](s: => Seq[L]): HListSingletons[L] =
    new HListSingletons[L] {
      def apply() = s
    }


  implicit val hnilSingletons: HListSingletons[HNil] =
    singletons(Seq(HNil))

  implicit def hconsSingletonsFound[H, T <: HList]
   (implicit
     headSingletons: Strict[Singletons[H]],
     tailSingletons: HListSingletons[T]
   ): HListSingletons[H :: T] =
    singletons {
      for {
        h <- headSingletons.value()
        t <- tailSingletons()
      } yield h :: t
    }
}

object CoproductSingletons {
  def apply[C <: Coproduct](implicit s: CoproductSingletons[C]): CoproductSingletons[C] = s

  def singletons[C <: Coproduct](s: => Seq[C]): CoproductSingletons[C] =
    new CoproductSingletons[C] {
      def apply() = s
    }

  implicit val cnilSingletons: CoproductSingletons[CNil] =
    singletons(Seq.empty)

  implicit def cconsSingletons[H, T <: Coproduct]
   (implicit
     headSingletons: Strict[Singletons[H]],
     tailSingletons: CoproductSingletons[T]
   ): CoproductSingletons[H :+: T] =
    singletons(headSingletons.value().map(Inl(_)) ++ tailSingletons().map(Inr(_)))
}

trait LowPrioritySingletons {
  /**
   * Fallback case if `T` cannot be built out of singletons.
   */
  implicit def singletonsNotFound[T]: Singletons[T] =
    Singletons.empty
}

object Singletons extends LowPrioritySingletons {
  def apply[T](implicit s: Singletons[T]): Singletons[T] = s

  def singletons[T](s: => Seq[T]): Singletons[T] =
    new Singletons[T] {
      def apply() = s
    }

  def empty[T]: Singletons[T] = singletons(Seq.empty)

  implicit def genericProductSingletons[P, L <: HList]
   (implicit
     gen: Generic.Aux[P, L],
     reprSingletons: Lazy[HListSingletons[L]]
   ): Singletons[P] =
    singletons(reprSingletons.value().map(gen.from))

  implicit def genericCoproductSingletons[S, C <: Coproduct]
   (implicit
     gen: Generic.Aux[S, C],
     reprSingletons: Lazy[CoproductSingletons[C]]
   ): Singletons[S] =
    singletons(reprSingletons.value().map(gen.from))

  implicit def hlistSingletons[L <: HList]
   (implicit
     underlying: HListSingletons[L]
   ): Singletons[L] =
    singletons(underlying())

  implicit def coproductSingletons[C <: Coproduct]
   (implicit
     underlying: CoproductSingletons[C]
   ): Singletons[C] =
    singletons(underlying())

  implicit def fieldTypeSingletons[K, H]
   (implicit
     underlying: Singletons[H]
   ): Singletons[FieldType[K, H]] =
    singletons(underlying().map(field[K](_)))

}
