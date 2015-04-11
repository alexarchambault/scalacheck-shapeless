package org.scalacheck

object Definitions {

  object T1 {
    sealed abstract class Tree
    final case class Node(left: Tree, right: Tree, v: Int) extends Tree
    case object Leaf extends Tree
  }

  object T2 {
    sealed abstract class Tree
    case class Node(left: Tree, right: Tree, v: Int) extends Tree
    case object Leaf extends Tree
  }

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

}

// Proxied type classes
object Instances {
  import Definitions._
  import Shapeless._

  implicit def t1Arbitrary = implicitly[Arbitrary[T1.Tree]]
  implicit def t2Arbitrary = implicitly[Arbitrary[T2.Tree]]

  implicit def emptyArbitrary = implicitly[Arbitrary[Empty.type]]
  implicit def emptyCCArbitrary = implicitly[Arbitrary[EmptyCC]]
  implicit def simpleArbitrary = implicitly[Arbitrary[Simple]]
  implicit def composedArbitrary = implicitly[Arbitrary[Composed]]
  implicit def twiceComposedArbitrary = implicitly[Arbitrary[TwiceComposed]]
  implicit def composedOptListArbitrary = implicitly[Arbitrary[ComposedOptList]]

  implicit def baseArbitrary = implicitly[Arbitrary[Base]]
  implicit def aArbitrary = implicitly[Arbitrary[A]]
  implicit def dArbitrary = implicitly[Arbitrary[D]]
}
