package org.scalacheck

import Definitions._
import Instances._

import shapeless.test.illTyped

/*
 * What we would need here is some kind of type-level property-based testing, in order to check
 * that a TC can be found for any case class or sealed hierarchy made of some base types
 * (Int, String, Boolean, ...), some composed types (List, Option, ...), and case classes /
 * sealed hierarchies of the same kind.
 *
 * As we don't have this, just checking that scalac can indeed find a TC for the few ones below.
 * These are far from exhaustive.
 *
 *
 * Testing the probability distributions of the resulting Arbitrary could be nice too.
 */

object Tests extends Properties("Tests") {
  private val ok = (_: Any) => true

  property("empty") = {
    Prop.forAll(Arbitrary.arbitrary[Empty.type])(ok)
  }

  property("empty case class") = {
    Prop.forAll(Arbitrary.arbitrary[EmptyCC])(ok)
  }

  property("simple case class") = {
    Prop.forAll(Arbitrary.arbitrary[Simple])(ok)
  }

  property("composed case class") = {
    Prop.forAll(Arbitrary.arbitrary[Composed])(ok)
  }

  property("twice composed case class") = {
    Prop.forAll(Arbitrary.arbitrary[TwiceComposed])(ok)
  }

  property("composed case class, with option and list") = {
    Prop.forAll(Arbitrary.arbitrary[ComposedOptList])(ok)
  }

  property("sum type") = {
    Prop.forAll(Arbitrary.arbitrary[Base])(ok)
  }

  illTyped(" implicitly[Arbitrary[NoArbitraryType]] ")
  illTyped(" implicitly[Arbitrary[ShouldHaveNoArb]] ")


  // These two make scalac almost diverge, especially the first one.
  // The few times I saw one of them converge, the result was the one I expected though.

  // illTyped(" implicitly[Arbitrary[ShouldHaveNoArbEither]] ")
  // illTyped(" implicitly[Arbitrary[BaseNoArb]] ")


  property("large hierarchy, base") = {
    Prop.forAll(Arbitrary.arbitrary[A])(ok)
  }

  property("large hierarchy, intermediate") = {
    Prop.forAll(Arbitrary.arbitrary[D])(ok)
  }

  /* property("large hierarchy, generate leaves") = {
    Prop.exists(Arbitrary.arbitrary[A]) {
      case _: B => true
      case _ => false
    }.flatMap { r => Prop(r.success) }
  }

  property("large hierarchy, generate leaves (other)") = {
    Prop.exists(Arbitrary.arbitrary[A]) {
      case F => true
      case _ => false
    }.flatMap { r => Prop(r.success) }
  } */
}
