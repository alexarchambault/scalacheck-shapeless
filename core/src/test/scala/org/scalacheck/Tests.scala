package org.scalacheck

import Definitions._
import Instances._

import shapeless.test.illTyped

import utest._
import Util._

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

object Tests extends TestSuite {
  private val ok = (_: Any) => true

  val tests = TestSuite {
    'empty {
      Prop.forAll(Arbitrary.arbitrary[Empty.type])(ok)
        .validate
    }

    'emptyCaseClass {
      Prop.forAll(Arbitrary.arbitrary[EmptyCC])(ok)
        .validate
    }

    'simpleCaseClass {
      Prop.forAll(Arbitrary.arbitrary[Simple])(ok)
        .validate
    }

    'composedCaseClass {
      Prop.forAll(Arbitrary.arbitrary[Composed])(ok)
        .validate
    }

    'twiceComposedCaseClass {
      Prop.forAll(Arbitrary.arbitrary[TwiceComposed])(ok)
        .validate
    }

    'composedCaseClassWithOptionAndList {
      Prop.forAll(Arbitrary.arbitrary[ComposedOptList])(ok)
        .validate
    }

    'sumType {
      Prop.forAll(Arbitrary.arbitrary[Base])(ok)
        .validate
    }

    illTyped(" implicitly[Arbitrary[NoArbitraryType]] ")
    illTyped(" implicitly[Arbitrary[ShouldHaveNoArb]] ")


    // These two make scalac almost diverge, especially the first one.
    // The few times I saw one of them converge, the result was the one I expected though.

    // illTyped(" implicitly[Arbitrary[ShouldHaveNoArbEither]] ")
    // illTyped(" implicitly[Arbitrary[BaseNoArb]] ")


    'largeHierarchyBase {
      Prop.forAll(Arbitrary.arbitrary[A])(ok)
        .validate
    }

    'largeHierarchyIntermediate {
      Prop.forAll(Arbitrary.arbitrary[D])(ok)
        .validate
    }

    /* 'large hierarchy, generate leaves") = {
      Prop.exists(Arbitrary.arbitrary[A]) {
        case _: B => true
        case _ => false
      }.flatMap { r => Prop(r.success) }
    }

    'large hierarchy, generate leaves (other)") = {
      Prop.exists(Arbitrary.arbitrary[A]) {
        case F => true
        case _ => false
      }.flatMap { r => Prop(r.success) }
    } */

    'listContainer {
      val arb = { import Shapeless._; Arbitrary.arbitrary[List[String]] }

      val n = 1000
      val empty = Iterator.fill(n)(arb.sample).count(_.exists(_.isEmpty)).toDouble / n

      Prop.propBoolean(empty > 0.001 && empty < 0.1)
        .validate
    }

    'optionContainer {
      val arb = { import Shapeless._; Arbitrary.arbitrary[Option[String]] }

      val n = 10000
      val empty = Iterator.fill(n)(arb.sample).count(_.exists(_.isEmpty)).toDouble / n

      Prop.propBoolean(empty > 0.001 && empty < 0.1)
        .validate
    }
  }

}
