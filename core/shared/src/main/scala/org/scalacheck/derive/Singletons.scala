package org.scalacheck.derive

import shapeless._
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

trait LowPrioritySingletons {
  /**
    * Fallback case if `T` cannot be built out of singletons.
    */
  implicit def singletonsNotFound[T]: Singletons[T] =
    Singletons.empty
}

object Singletons extends LowPrioritySingletons {
  def apply[T](implicit s: Singletons[T]): Singletons[T] = s

  def instance[T](s: => Seq[T]): Singletons[T] =
    new Singletons[T] {
      def apply() = s
    }

  def empty[T]: Singletons[T] = instance(Seq.empty)

  implicit def genericProduct[P, L <: HList]
   (implicit
     gen: Generic.Aux[P, L],
     reprSingletons: Lazy[HListSingletons[L]]
   ): Singletons[P] =
    instance(reprSingletons.value().map(gen.from))

  implicit def genericCoproduct[S, C <: Coproduct]
   (implicit
     gen: Generic.Aux[S, C],
     reprSingletons: Lazy[CoproductSingletons[C]]
   ): Singletons[S] =
    instance(reprSingletons.value().map(gen.from))

  implicit def hlist[L <: HList]
   (implicit
     underlying: HListSingletons[L]
   ): Singletons[L] =
    instance(underlying())

  implicit def coproduct[C <: Coproduct]
   (implicit
     underlying: CoproductSingletons[C]
   ): Singletons[C] =
    instance(underlying())

  implicit def fieldType[K, H]
   (implicit
     underlying: Singletons[H]
   ): Singletons[FieldType[K, H]] =
    instance(underlying().map(field[K](_)))
}

trait HListSingletons[L <: HList] {
  /**
   * Instances of `L` that can be built out of singletons, or
   * an empty sequence if none were found.
   */
  def apply(): Seq[L]
}

object HListSingletons {
  def apply[L <: HList](implicit s: HListSingletons[L]): HListSingletons[L] = s

  def instance[L <: HList](s: => Seq[L]): HListSingletons[L] =
    new HListSingletons[L] {
      def apply() = s
    }


  implicit val hnil: HListSingletons[HNil] =
    instance(Seq(HNil))

  implicit def hconsFound[H, T <: HList]
   (implicit
     headSingletons: Strict[Singletons[H]],
     tailSingletons: HListSingletons[T]
   ): HListSingletons[H :: T] =
    instance {
      for {
        h <- headSingletons.value()
        t <- tailSingletons()
      } yield h :: t
    }
}

trait CoproductSingletons[C <: Coproduct] {
  /**
    * Instances of `C` that can be built out of singletons, or
    * an empty sequence if none were found.
    */
  def apply(): Seq[C]
}

object CoproductSingletons {
  def apply[C <: Coproduct](implicit s: CoproductSingletons[C]): CoproductSingletons[C] = s

  def instance[C <: Coproduct](s: => Seq[C]): CoproductSingletons[C] =
    new CoproductSingletons[C] {
      def apply() = s
    }

  implicit val cnil: CoproductSingletons[CNil] =
    instance(Seq.empty)

  implicit def ccons[H, T <: Coproduct]
   (implicit
     headSingletons: Strict[Singletons[H]],
     tailSingletons: CoproductSingletons[T]
   ): CoproductSingletons[H :+: T] =
    instance(headSingletons.value().map(Inl(_)) ++ tailSingletons().map(Inr(_)))
}
