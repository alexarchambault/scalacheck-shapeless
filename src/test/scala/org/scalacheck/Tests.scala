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


// cvogt's hierarchy
sealed trait A
sealed case class B(i: Int, s: String) extends A
case object C extends A
sealed trait D extends A
final case class E(a: Double, b: Option[Float]) extends D
case object F extends D
sealed abstract class Foo extends D
case object Baz extends Foo
/*final class Bar extends Foo
final class Baz(i1: Int)(s1: String) extends Foo*/


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
