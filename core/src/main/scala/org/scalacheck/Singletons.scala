package org.scalacheck

import shapeless._

trait Singletons[T] {
  def apply(): Seq[T]
}

trait LowPrioritySingletons {
  implicit def hconsSingletonsNotFound[H, T <: HList]
   (implicit
     tailSingletons: Lazy[Singletons[T]]
   ): Singletons[H :: T] =
    Singletons.empty
}

object Singletons extends LowPrioritySingletons {
  def apply[T](implicit s: Singletons[T]): Singletons[T] = s

  def singletons[T](s: => Seq[T]): Singletons[T] =
    new Singletons[T] {
      def apply() = s
    }

  def empty[T]: Singletons[T] = singletons(Seq.empty)


  implicit val hnilSingletons: Singletons[HNil] =
    singletons(Seq(HNil))

  implicit def hconsSingletonsFound[H, T <: HList]
   (implicit
     headSingletons: Lazy[Singletons[H]],
     tailSingletons: Lazy[Singletons[T]]
   ): Singletons[H :: T] =
    singletons {
      for {
        h <- headSingletons.value()
        t <- tailSingletons.value()
      } yield h :: t
    }

  implicit val cnilSingletons: Singletons[CNil] =
    empty

  implicit def cconsSingletons[H, T <: Coproduct]
   (implicit
     headSingletons: Lazy[Singletons[H]],
     tailSingletons: Lazy[Singletons[T]]
   ): Singletons[H :+: T] =
    singletons(headSingletons.value().map(Inl(_)) ++ tailSingletons.value().map(Inr(_)))

  implicit def instanceSingletons[F, G]
   (implicit
     gen: Generic.Aux[F, G],
     reprSingletons: Lazy[Singletons[G]]
   ): Singletons[F] =
    singletons(reprSingletons.value().map(gen.from))

}
