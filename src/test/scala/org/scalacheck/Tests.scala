package org.scalacheck

import Shapeless._
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

/*
 * We should have Arbitrary instances for these
 */
case object Empty
case class EmptyCC()
case class Simple(i: Int, s: String, blah: Boolean)
case class Composed(foo: Simple, other: String)
case class TwiceComposed(foo: Simple, bar: Composed, v: Int)
case class ComposedOptList(fooOpt: Option[Simple], other: String, l: List[TwiceComposed])

case class NowThree(s: String, i: Int, n: Double)

sealed trait Base
case class BaseIS(i: Int, s: String) extends Base
case class BaseDB(d: Double, b: Boolean) extends Base
case class BaseLast(c: Simple) extends Base


/*
 * We should *not* have Arbitrary instances for these
 */
trait NoArbitraryType
case class ShouldHaveNoArb(n: NoArbitraryType, i: Int)
case class ShouldHaveNoArbEither(s: String, i: Int, n: NoArbitraryType)

sealed trait BaseNoArb
case class BaseNoArbIS(i: Int, s: String) extends BaseNoArb
case class BaseNoArbDB(d: Double, b: Boolean) extends BaseNoArb
case class BaseNoArbN(n: NoArbitraryType) extends BaseNoArb


object Tests {
  implicitly[Arbitrary[Empty.type]]
  implicitly[Arbitrary[EmptyCC]]
  implicitly[Arbitrary[Simple]]
  implicitly[Arbitrary[Composed]]
  implicitly[Arbitrary[TwiceComposed]]
  implicitly[Arbitrary[ComposedOptList]]

  implicitly[Arbitrary[NowThree]]

  implicitly[Arbitrary[Base]]

  illTyped(" implicitly[Arbitrary[NoArbitraryType]] ")
  illTyped(" implicitly[Arbitrary[ShouldHaveNoArb]] ")


  // These two make scalac almost diverge, especially the first one.
  // The few times I saw one of them converge, the result was the one I expected though.

  // illTyped(" implicitly[Arbitrary[ShouldHaveNoArbEither]] ")
  // illTyped(" implicitly[Arbitrary[BaseNoArb]] ")
}
