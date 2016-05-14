package org.scalacheck

import org.scalacheck.derive._

import shapeless.{ Lazy => _, _ }
import shapeless.compat._
import shapeless.labelled.FieldType
import shapeless.record._
import shapeless.union._
import shapeless.test.illTyped

import utest._

import Util._

object TestsDefinitions {

  case class Simple(i: Int, s: String, blah: Boolean)

  case object Empty
  case class EmptyCC()
  case class Composed(foo: Simple, other: String)
  case class TwiceComposed(foo: Simple, bar: Composed, v: Int)
  case class ComposedOptList(fooOpt: Option[Simple], other: String, l: List[TwiceComposed])

  sealed trait Base
  case class BaseIS(i: Int, s: String) extends Base
  case class BaseDB(d: Double, b: Boolean) extends Base
  case class BaseLast(c: Simple) extends Base

  case class CCWithSingleton(i: Int, s: Witness.`"aa"`.T)

  sealed trait BaseWithSingleton
  object BaseWithSingleton {
    case class Main(s: Witness.`"aa"`.T) extends BaseWithSingleton
    case class Dummy(i: Int) extends BaseWithSingleton
  }

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

  // cvogt's hierarchy
  sealed trait A
  sealed case class B(i: Int, s: String) extends A
  case object C extends A
  sealed trait D extends A
  final case class E(a: Double, b: Option[Float]) extends D
  case object F extends D
  sealed abstract class Foo extends D
  case object Baz extends Foo
  // Not supporting this one
  // final class Bar extends Foo
  // final class Baz(i1: Int)(s1: String) extends Foo

  type L = Int :: String :: HNil
  type Rec = Record.`'i -> Int, 's -> String`.T

  type C0 = Int :+: String :+: CNil
  type Un = Union.`'i -> Int, 's -> String`.T


  object NoTCDefinitions {
    trait NoArbitraryType
    case class ShouldHaveNoArb(n: NoArbitraryType, i: Int)
    case class ShouldHaveNoArbEither(s: String, i: Int, n: NoArbitraryType)

    sealed trait BaseNoArb
    case class BaseNoArbIS(i: Int, s: String) extends BaseNoArb
    case class BaseNoArbDB(d: Double, b: Boolean) extends BaseNoArb
    case class BaseNoArbN(n: NoArbitraryType) extends BaseNoArb
  }

}

object ArbitraryTests extends TestSuite {
  import TestsDefinitions._
  import Shapeless._


  lazy val expectedSimpleArb =
    MkArbitrary.genericProduct(
      Generic[Simple],
      MkHListArbitrary.hcons(
        Arbitrary.arbInt,
        MkHListArbitrary.hcons(
          Arbitrary.arbString,
          MkHListArbitrary.hcons(
            Arbitrary.arbBool,
            MkHListArbitrary.hnil,
            ops.hlist.Length[HNil],
            ops.nat.ToInt[Nat._0]
          ),
          ops.hlist.Length[Boolean :: HNil],
          ops.nat.ToInt[Nat._1]
        ),
        ops.hlist.Length[String :: Boolean :: HNil],
        ops.nat.ToInt[Nat._2]
      )
    ).arbitrary

  lazy val expectedIntStringBoolArb =
    MkHListArbitrary.hcons(
      Arbitrary.arbInt,
      MkHListArbitrary.hcons(
        Arbitrary.arbString,
        MkHListArbitrary.hcons(
          Arbitrary.arbBool,
          MkHListArbitrary.hnil,
          ops.hlist.Length[HNil],
          ops.nat.ToInt[Nat._0]
        ),
        ops.hlist.Length[Boolean :: HNil],
        ops.nat.ToInt[Nat._1]
      ),
      ops.hlist.Length[String :: Boolean :: HNil],
      ops.nat.ToInt[Nat._2]
    ).arbitrary

  lazy val expectedIntStringBoolCoproductArb =
    MkCoproductArbitrary.ccons(
      Arbitrary.arbInt,
      MkCoproductArbitrary.ccons(
        Arbitrary.arbString,
        MkCoproductArbitrary.ccons(
          Arbitrary.arbBool,
          MkCoproductArbitrary.cnil,
          ops.coproduct.Length[CNil],
          ops.nat.ToInt[Nat._0]
        ),
        ops.coproduct.Length[Boolean :+: CNil],
        ops.nat.ToInt[Nat._1]
      ),
      ops.coproduct.Length[String :+: Boolean :+: CNil],
      ops.nat.ToInt[Nat._2]
    ).arbitrary

  lazy val expectedComposedArb =
    MkArbitrary.genericProduct(
      Generic[Composed],
      MkHListArbitrary.hcons(
        expectedSimpleArb,
        MkHListArbitrary.hcons(
          Arbitrary.arbString,
          MkHListArbitrary.hnil,
          ops.hlist.Length[HNil],
          ops.nat.ToInt[Nat._0]
        ),
        ops.hlist.Length[String :: HNil],
        ops.nat.ToInt[Nat._1]
      )
    ).arbitrary

  lazy val expectedTwiceComposedArb =
    MkArbitrary.genericProduct(
      Generic[TwiceComposed],
      MkHListArbitrary.hcons(
        expectedSimpleArb,
        MkHListArbitrary.hcons(
          expectedComposedArb,
          MkHListArbitrary.hcons(
            Arbitrary.arbInt,
            MkHListArbitrary.hnil,
            ops.hlist.Length[HNil],
            ops.nat.ToInt[Nat._0]
          ),
          ops.hlist.Length[Int :: HNil],
          ops.nat.ToInt[Nat._1]
        ),
        ops.hlist.Length[Composed :: Int :: HNil],
        ops.nat.ToInt[Nat._2]
      )
    ).arbitrary

  lazy val expectedComposedOptListArb =
    MkArbitrary.genericProduct(
      Generic[ComposedOptList],
      MkHListArbitrary.hcons(
        Arbitrary.arbOption(expectedSimpleArb),
        MkHListArbitrary.hcons(
          Arbitrary.arbString,
          MkHListArbitrary.hcons(
            Arbitrary.arbContainer[List, TwiceComposed](expectedTwiceComposedArb, implicitly, identity),
            MkHListArbitrary.hnil,
            ops.hlist.Length[HNil],
            ops.nat.ToInt[Nat._0]
          ),
          ops.hlist.Length[List[TwiceComposed] :: HNil],
          ops.nat.ToInt[Nat._1]
        ),
        ops.hlist.Length[String :: List[TwiceComposed] :: HNil],
        ops.nat.ToInt[Nat._2]
      )
    ).arbitrary

  lazy val expectedBaseArb =
    MkArbitrary.genericCoproduct(
      Generic[Base],
      MkCoproductArbitrary.ccons(
        MkArbitrary.genericProduct(
          Generic[BaseDB],
          MkHListArbitrary.hcons(
            Arbitrary.arbDouble,
            MkHListArbitrary.hcons(
              Arbitrary.arbBool,
              MkHListArbitrary.hnil,
              ops.hlist.Length[HNil],
              ops.nat.ToInt[Nat._0]
            ),
            ops.hlist.Length[Boolean :: HNil],
            ops.nat.ToInt[Nat._1]
          )
        ).arbitrary,
        MkCoproductArbitrary.ccons(
          MkArbitrary.genericProduct(
            Generic[BaseIS],
            MkHListArbitrary.hcons(
              Arbitrary.arbInt,
              MkHListArbitrary.hcons(
                Arbitrary.arbString,
                MkHListArbitrary.hnil,
                ops.hlist.Length[HNil],
                ops.nat.ToInt[Nat._0]
              ),
              ops.hlist.Length[String :: HNil],
              ops.nat.ToInt[Nat._1]
            )
          ).arbitrary,
          MkCoproductArbitrary.ccons(
            MkArbitrary.genericProduct(
              Generic[BaseLast],
              MkHListArbitrary.hcons(
                expectedSimpleArb,
                MkHListArbitrary.hnil,
                ops.hlist.Length[HNil],
                ops.nat.ToInt[Nat._0]
              )
            ).arbitrary,
            MkCoproductArbitrary.cnil,
            ops.coproduct.Length[CNil],
            ops.nat.ToInt[Nat._0]
          ),
          ops.coproduct.Length[BaseLast :+: CNil],
          ops.nat.ToInt[Nat._1]
        ),
        ops.coproduct.Length[BaseIS :+: BaseLast :+: CNil],
        ops.nat.ToInt[Nat._2]
      )
    ).arbitrary

  lazy val expectedCCWithSingletonArb =
    MkArbitrary.genericProduct(
      Generic[CCWithSingleton],
      MkHListArbitrary.hcons(
        Arbitrary.arbInt,
        MkHListArbitrary.hcons(
          Shapeless.arbitrarySingletonType[Witness.`"aa"`.T],
          MkHListArbitrary.hnil,
          ops.hlist.Length[HNil],
          ops.nat.ToInt[Nat._0]
        ),
        ops.hlist.Length[Witness.`"aa"`.T :: HNil],
        ops.nat.ToInt[Nat._1]
      )
    ).arbitrary

  lazy val expectedBaseWithSingletonMainArb =
    MkArbitrary.genericProduct(
      Generic[BaseWithSingleton.Main],
      MkHListArbitrary.hcons(
        Shapeless.arbitrarySingletonType[Witness.`"aa"`.T],
        MkHListArbitrary.hnil,
        ops.hlist.Length[HNil],
        ops.nat.ToInt[Nat._0]
      )
    ).arbitrary

  lazy val expectedBaseWithSingletonDummyArb =
    MkArbitrary.genericProduct(
      Generic[BaseWithSingleton.Dummy],
      MkHListArbitrary.hcons(
        Arbitrary.arbInt,
        MkHListArbitrary.hnil,
        ops.hlist.Length[HNil],
        ops.nat.ToInt[Nat._0]
      )
    ).arbitrary

  lazy val expectedBaseWithSingletonArb =
    MkArbitrary.genericCoproduct(
      Generic[BaseWithSingleton],
      MkCoproductArbitrary.ccons(
        expectedBaseWithSingletonDummyArb,
        MkCoproductArbitrary.ccons(
          expectedBaseWithSingletonMainArb,
          MkCoproductArbitrary.cnil,
          ops.coproduct.Length[CNil],
          ops.nat.ToInt[Nat._0]
        ),
        ops.coproduct.Length[BaseWithSingleton.Main :+: CNil],
        ops.nat.ToInt[Nat._1]
      )
    ).arbitrary

  lazy val expectedT1TreeArbitrary: Arbitrary[T1.Tree] =
    MkArbitrary.genericCoproduct(
      Generic[T1.Tree],
      MkCoproductArbitrary.ccons(
        MkArbitrary.genericProduct(
          Generic[T1.Leaf.type],
          Lazy(
            MkHListArbitrary.hnil
          )
        ).arbitrary,
        MkCoproductArbitrary.ccons(
          MkArbitrary.genericProduct(
            Generic[T1.Node],
            MkHListArbitrary.hcons(
              expectedT1TreeArbitrary,
              MkHListArbitrary.hcons(
                expectedT1TreeArbitrary,
                MkHListArbitrary.hcons(
                  Arbitrary.arbInt,
                  MkHListArbitrary.hnil,
                  ops.hlist.Length[HNil],
                  ops.nat.ToInt[Nat._0]
                ),
                ops.hlist.Length[Int :: HNil],
                ops.nat.ToInt[Nat._1]
              ),
              ops.hlist.Length[T1.Tree :: Int :: HNil],
              ops.nat.ToInt[Nat._2]
            )
          ).arbitrary,
          MkCoproductArbitrary.cnil,
          ops.coproduct.Length[CNil],
          ops.nat.ToInt[Nat._0]
        ),
        ops.coproduct.Length[T1.Node :+: CNil],
        ops.nat.ToInt[Nat._1]
      )
    ).arbitrary

  lazy val expectedT2TreeArbitrary: Arbitrary[T2.Tree] =
    MkArbitrary.genericCoproduct(
      Generic[T2.Tree],
      MkCoproductArbitrary.ccons(
        MkArbitrary.genericProduct(
          Generic[T2.Leaf.type],
          MkHListArbitrary.hnil
        ).arbitrary,
        MkCoproductArbitrary.ccons(
          MkArbitrary.genericProduct(
            Generic[T2.Node],
            MkHListArbitrary.hcons(
              expectedT2TreeArbitrary,
              MkHListArbitrary.hcons(
                expectedT2TreeArbitrary,
                MkHListArbitrary.hcons(
                  Arbitrary.arbInt,
                  MkHListArbitrary.hnil,
                  ops.hlist.Length[HNil],
                  ops.nat.ToInt[Nat._0]
                ),
                ops.hlist.Length[Int :: HNil],
                ops.nat.ToInt[Nat._1]
              ),
              ops.hlist.Length[T2.Tree :: Int :: HNil],
              ops.nat.ToInt[Nat._2]
            )
          ).arbitrary,
          MkCoproductArbitrary.cnil,
          ops.coproduct.Length[CNil],
          ops.nat.ToInt[Nat._0]
        ),
        ops.coproduct.Length[T2.Node :+: CNil],
        ops.nat.ToInt[Nat._1]
      )
    ).arbitrary

  lazy val expectedBazArbitrary =
    MkArbitrary.genericProduct(
      Generic[Baz.type],
      MkHListArbitrary.hnil
    ).arbitrary

  lazy val expectedFooArbitrary =
    MkArbitrary.genericCoproduct(
      Generic[Foo],
      MkCoproductArbitrary.ccons(
        expectedBazArbitrary,
        MkCoproductArbitrary.cnil,
        ops.coproduct.Length[CNil],
        ops.nat.ToInt[Nat._0]
      )
    ).arbitrary

  lazy val expectedFArbitrary =
    MkArbitrary.genericProduct(
      Generic[F.type],
      MkHListArbitrary.hnil
    ).arbitrary

  lazy val expectedEArbitrary =
    MkArbitrary.genericProduct(
      Generic[E],
      MkHListArbitrary.hcons(
        Arbitrary.arbDouble,
        MkHListArbitrary.hcons(
          Arbitrary.arbOption(Arbitrary.arbFloat),
          MkHListArbitrary.hnil,
          ops.hlist.Length[HNil],
          ops.nat.ToInt[Nat._0]
        ),
        ops.hlist.Length[Option[Float] :: HNil],
        ops.nat.ToInt[Nat._1]
      )
    ).arbitrary

  lazy val expectedDArbitrary =
    MkArbitrary.genericCoproduct(
      Generic[D],
      MkCoproductArbitrary.ccons(
        expectedBazArbitrary,
        MkCoproductArbitrary.ccons(
          expectedEArbitrary,
          MkCoproductArbitrary.ccons(
            expectedFArbitrary,
            MkCoproductArbitrary.cnil,
            ops.coproduct.Length[CNil],
            ops.nat.ToInt[Nat._0]
          ),
          ops.coproduct.Length[F.type :+: CNil],
          ops.nat.ToInt[Nat._1]
        ),
        ops.coproduct.Length[E :+: F.type :+: CNil],
        ops.nat.ToInt[Nat._2]
      )
    ).arbitrary

  lazy val expectedCArbitrary =
    MkArbitrary.genericProduct(
      Generic[C.type],
      MkHListArbitrary.hnil
    ).arbitrary

  lazy val expectedBArbitrary =
    MkArbitrary.genericProduct(
      Generic[B],
      MkHListArbitrary.hcons(
        Arbitrary.arbInt,
        MkHListArbitrary.hcons(
          Arbitrary.arbString,
          MkHListArbitrary.hnil,
          ops.hlist.Length[HNil],
          ops.nat.ToInt[Nat._0]
        ),
        ops.hlist.Length[String :: HNil],
        ops.nat.ToInt[Nat._1]
      )
    ).arbitrary

  lazy val expectedAArbitrary =
    MkArbitrary.genericCoproduct(
      Generic[A],
      MkCoproductArbitrary.ccons(
        expectedBArbitrary,
        MkCoproductArbitrary.ccons(
          expectedBazArbitrary,
          MkCoproductArbitrary.ccons(
            expectedCArbitrary,
            MkCoproductArbitrary.ccons(
              expectedEArbitrary,
              MkCoproductArbitrary.ccons(
                expectedFArbitrary,
                MkCoproductArbitrary.cnil,
                ops.coproduct.Length[CNil],
                ops.nat.ToInt[Nat._0]
              ),
              ops.coproduct.Length[F.type :+: CNil],
              ops.nat.ToInt[Nat._1]
            ),
            ops.coproduct.Length[E :+: F.type :+: CNil],
            ops.nat.ToInt[Nat._2]
          ),
          ops.coproduct.Length[C.type :+: E :+: F.type :+: CNil],
          ops.nat.ToInt[Nat._3]
        ),
        ops.coproduct.Length[Baz.type :+: C.type :+: E :+: F.type :+: CNil],
        ops.nat.ToInt[Nat._4]
      )
    ).arbitrary

  lazy val expectetLArbitrary =
    MkHListArbitrary.hcons(
      Arbitrary.arbInt,
      MkHListArbitrary.hcons(
        Arbitrary.arbString,
        MkHListArbitrary.hnil,
        ops.hlist.Length.hnilLength[HNil],
        ops.nat.ToInt[Nat._0]
      ),
      ops.hlist.Length.hlistLength[String, HNil, Nat._0],
      ops.nat.ToInt[Nat._1]
    ).arbitrary

  lazy val expectetRecArbitrary: Arbitrary[Rec] =
    MkHListArbitrary.hcons[FieldType[Witness.`'i`.T, Int], Record.`'s -> String`.T, Nat._1](
      arbitraryFieldType[Witness.`'i`.T, Int](Arbitrary.arbInt),
      MkHListArbitrary.hcons[FieldType[Witness.`'s`.T, String], HNil, Nat._0](
        arbitraryFieldType[Witness.`'s`.T, String](Arbitrary.arbString),
        MkHListArbitrary.hnil,
        ops.hlist.Length.hnilLength[HNil],
        ops.nat.ToInt[Nat._0]
      ),
      ops.hlist.Length.hlistLength[FieldType[Witness.`'s`.T, String], HNil, Nat._0],
      ops.nat.ToInt[Nat._1]
    ).arbitrary

  lazy val expectetC0Arbitrary =
    MkCoproductArbitrary.ccons(
      Arbitrary.arbInt,
      MkCoproductArbitrary.ccons(
        Arbitrary.arbString,
        MkCoproductArbitrary.cnil,
        ops.coproduct.Length.cnilLength,
        ops.nat.ToInt[Nat._0]
      ),
      ops.coproduct.Length.coproductLength[String, CNil, Nat._0],
      ops.nat.ToInt[Nat._1]
    ).arbitrary

  lazy val expectedUnionArbitrary: Arbitrary[Un] =
    MkCoproductArbitrary.ccons[FieldType[Witness.`'i`.T, Int], Union.`'s -> String`.T, Nat._1](
      arbitraryFieldType[Witness.`'i`.T, Int](Arbitrary.arbInt),
      MkCoproductArbitrary.ccons[FieldType[Witness.`'s`.T, String], CNil, Nat._0](
        arbitraryFieldType[Witness.`'s`.T, String](Arbitrary.arbString),
        MkCoproductArbitrary.cnil,
        ops.coproduct.Length.cnilLength,
        ops.nat.ToInt[Nat._0]
      ),
      ops.coproduct.Length.coproductLength[FieldType[Witness.`'s`.T, String], CNil, Nat._0],
      ops.nat.ToInt[Nat._1]
    ).arbitrary


  val tests = TestSuite {

    'compareSuccess - {
      val arb = Arbitrary.arbInt.arbitrary
      compareArbitrary(arb, arb)
    }

    'compareFailure - {
      val arb = Arbitrary.arbInt
      val result =
        try {
          compareArbitrary(arb.arbitrary, arb.arbitrary.map(_ + 1))
          false
        }
        catch {
          case _: utest.AssertionError => true
        }

      assert(result)
    }

    'empty - {
      val expectedArb =
        MkArbitrary.genericProduct(
          Generic[Empty.type],
          Lazy(MkHListArbitrary.hnil)
        ).arbitrary

      val gen = Arbitrary.arbitrary[Empty.type]
      compareArbitrary(expectedArb.arbitrary, gen)
    }

    'emptyCC - {
      val expectedArb =
        MkArbitrary.genericProduct(
          Generic[EmptyCC],
          Lazy(MkHListArbitrary.hnil)
        ).arbitrary

      val gen = Arbitrary.arbitrary[EmptyCC]
      compareArbitrary(expectedArb.arbitrary, gen)
    }

    'simple - {
      val gen = Arbitrary.arbitrary[Simple]
      compareArbitrary(expectedSimpleArb.arbitrary, gen)
    }

    'simpleHList - {
      val gen = Arbitrary.arbitrary[Int :: String :: Boolean :: HNil]
      compareArbitrary(expectedIntStringBoolArb.arbitrary, gen)
    }

    'simpleCoproduct - {
      val gen = Arbitrary.arbitrary[Int :+: String :+: Boolean :+: CNil]
      compareArbitrary(expectedIntStringBoolCoproductArb.arbitrary, gen)
    }

    'composed - {
      val gen = Arbitrary.arbitrary[Composed]
      compareArbitrary(expectedComposedArb.arbitrary, gen)
    }

    'twiceComposed - {
      val gen = Arbitrary.arbitrary[TwiceComposed]
      compareArbitrary(expectedTwiceComposedArb.arbitrary, gen)
    }

    'composedOptList - {
      val gen = Arbitrary.arbitrary[ComposedOptList]
      compareArbitrary(expectedComposedOptListArb.arbitrary, gen)
    }

    'base - {
      val gen = Arbitrary.arbitrary[Base]
      compareArbitrary(expectedBaseArb.arbitrary, gen)
    }

    'tree1 - {
      val gen = Arbitrary.arbitrary[T1.Tree]
      compareArbitrary(expectedT1TreeArbitrary.arbitrary, gen)
    }

    'tree2 - {
      val gen = Arbitrary.arbitrary[T2.Tree]
      compareArbitrary(expectedT2TreeArbitrary.arbitrary, gen)
    }

    'a - {
      val gen = Arbitrary.arbitrary[A]
      compareArbitrary(expectedAArbitrary.arbitrary, gen)
    }

    'd - {
      val gen = Arbitrary.arbitrary[D]
      compareArbitrary(expectedDArbitrary.arbitrary, gen)
    }

    'list - {
      val expected = Arbitrary.arbContainer[List, Int](Arbitrary.arbInt, implicitly, identity)
      val gen = Arbitrary.arbitrary[List[Int]]
      compareArbitrary(expected.arbitrary, gen)
    }

    'option - {
      val expected = Arbitrary.arbOption(Arbitrary.arbInt)
      val gen = Arbitrary.arbitrary[Option[Int]]
      compareArbitrary(expected.arbitrary, gen)
    }

    'singleton - {
      'simple - {
        val expected = Arbitrary(Gen.const(2: Witness.`2`.T))
        val gen = Arbitrary.arbitrary[Witness.`2`.T]
        compareArbitrary(expected.arbitrary, gen)
      }

      'caseClass - {
        val gen = Arbitrary.arbitrary[CCWithSingleton]
        compareArbitrary(expectedCCWithSingletonArb.arbitrary, gen)
      }

      'ADT - {
        val gen = Arbitrary.arbitrary[BaseWithSingleton]
        compareArbitrary(expectedBaseWithSingletonArb.arbitrary, gen)
      }
    }

    'shapeless - {
      'hlist - {
        val gen = Arbitrary.arbitrary[L]
        compareArbitrary(expectetLArbitrary.arbitrary, gen)
      }

      'record - {
        val gen = Arbitrary.arbitrary[Rec]
        compareArbitrary(expectetRecArbitrary.arbitrary, gen)
      }

      'copproduct - {
        val gen = Arbitrary.arbitrary[C0]
        compareArbitrary(expectetC0Arbitrary.arbitrary, gen)
      }

      'union - {
        val gen = Arbitrary.arbitrary[Un]
        compareArbitrary(expectedUnionArbitrary.arbitrary, gen)
      }
    }

  }

  object NoTC {
    import NoTCDefinitions._

    illTyped("""
      Arbitrary.arbitrary[NoArbitraryType]
    """)

    illTyped("""
      Arbitrary.arbitrary[ShouldHaveNoArb]
    """)

    illTyped("""
      Arbitrary.arbitrary[ShouldHaveNoArbEither]
    """)

    illTyped("""
      Arbitrary.arbitrary[BaseNoArb]
    """)
  }

}
