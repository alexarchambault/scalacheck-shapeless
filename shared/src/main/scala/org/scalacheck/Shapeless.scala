package org.scalacheck

import shapeless._

import derive._


object Shapeless {

  implicit def mkArbitrary[T]
   (implicit
     priority: Strict.Cached[LowPriority[
       Arbitrary[T],
       MkDefaultArbitrary[T]
     ]]
   ): Arbitrary[T] =
    priority.value.value.arbitrary

  implicit def mkShrink[T]
   (implicit
     priority: Strict.Cached[LowPriority[
       Shrink[T],
       MkDefaultShrink[T]
     ]]
   ): Shrink[T] =
    priority.value.value.shrink

  implicit def mkCogen[T]
   (implicit
     priority: Strict.Cached[LowPriority[
       Cogen[T],
       MkDefaultCogen[T]
     ]]
   ): Cogen[T] =
    priority.value.value.cogen

}
