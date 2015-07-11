package org.scalacheck

import shapeless._

import derive._


object Shapeless {

  implicit def mkArbitrary[T]
   (implicit
     priority: Lazy[Priority[
       Arbitrary[T],
       Implicit[
         MkSingletonArbitrary[T] :+:
         MkDefaultArbitrary[T] :+: CNil
       ]
     ]]
   ): Arbitrary[T] =
    priority.value.fold(identity)(impl => impl.value.unify.arbitrary)

  implicit def mkShrink[T]
   (implicit
     priority: Lazy[Priority[
       Shrink[T],
       Implicit[
         MkDefaultShrink[T] :+: CNil
       ]
     ]]
   ): Shrink[T] =
    priority.value.fold(identity)(impl => impl.value.unify.shrink)

  implicit def mkCogen[T]
   (implicit
     priority: Lazy[Priority[
       Cogen[T],
       Implicit[
         MkDefaultCogen[T] :+: CNil
       ]
     ]]
   ): Cogen[T] =
    priority.value.fold(identity)(impl => impl.value.unify.cogen)

}
