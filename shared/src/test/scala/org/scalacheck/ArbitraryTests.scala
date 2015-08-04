package org.scalacheck

import org.scalacheck.Gen.Parameters
import org.scalacheck.derive.{MkCoproductArbitrary, MkHListArbitrary, MkArbitrary}
import org.scalacheck.rng.{ Seed, Rng }

import shapeless._
import shapeless.test.illTyped

import utest._

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

  def stream[T](parameters: Parameters, seed: Seed)(arbitrary: Gen[T]): Stream[Option[T]] = {
    def helper(seed: Seed): Stream[Option[T]] = {
      val r = arbitrary.doApply(parameters, seed)
      r.retrieve #:: helper(r.seed)
    }

    helper(seed)
  }

  def doCompare[T](
    parameters: Parameters,
    seed: Seed )(
    first: Gen[T],
    second: Gen[T] )(
    len: Int
  ): Unit = {
    val generated =
      stream(parameters, seed)(first)
        .zip(stream(parameters, seed)(second))
        .take(len)

    assert(generated.forall{case (a, b) => a == b})
  }

  /** Ask each `Gen[T]` a sequence of values, given the same parameters and initial seed,
    * and throw an exception if both sequences aren't equal. */
  def compare[T](first: Gen[T], second: Gen[T]): Unit =
    doCompare(Parameters.default, Rng.randomSeed())(first, second)(100)


  lazy val expectedSimpleArb =
    MkArbitrary.genericProductMkArb(
      Generic[Simple],
      Lazy(
        MkHListArbitrary.hconsMkArb(
          Lazy(Arbitrary.arbInt),
          MkHListArbitrary.hconsMkArb(
            Lazy(Arbitrary.arbString),
            MkHListArbitrary.hconsMkArb(
              Lazy(Arbitrary.arbBool),
              MkHListArbitrary.hnilMkArb,
              ops.hlist.Length[HNil],
              ops.nat.ToInt[Nat._0]
            ),
            ops.hlist.Length[Boolean :: HNil],
            ops.nat.ToInt[Nat._1]
          ),
          ops.hlist.Length[String :: Boolean :: HNil],
          ops.nat.ToInt[Nat._2]
        )
      )
    ).arbitrary

  lazy val expectedIntStringBoolArb =
    MkHListArbitrary.hconsMkArb(
      Lazy(Arbitrary.arbInt),
      MkHListArbitrary.hconsMkArb(
        Lazy(Arbitrary.arbString),
        MkHListArbitrary.hconsMkArb(
          Lazy(Arbitrary.arbBool),
          MkHListArbitrary.hnilMkArb,
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
    MkCoproductArbitrary.cconsMkArb(
      Lazy(Arbitrary.arbInt),
      MkCoproductArbitrary.cconsMkArb(
        Lazy(Arbitrary.arbString),
        MkCoproductArbitrary.cconsMkArb(
          Lazy(Arbitrary.arbBool),
          MkCoproductArbitrary.cnilMkArb,
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
    MkArbitrary.genericProductMkArb(
      Generic[Composed],
      Lazy(
        MkHListArbitrary.hconsMkArb(
          Lazy(expectedSimpleArb),
          MkHListArbitrary.hconsMkArb(
            Lazy(Arbitrary.arbString),
            MkHListArbitrary.hnilMkArb,
            ops.hlist.Length[HNil],
            ops.nat.ToInt[Nat._0]
          ),
          ops.hlist.Length[String :: HNil],
          ops.nat.ToInt[Nat._1]
        )
      )
    ).arbitrary

  lazy val expectedTwiceComposedArb =
    MkArbitrary.genericProductMkArb(
      Generic[TwiceComposed],
      Lazy(
        MkHListArbitrary.hconsMkArb(
          Lazy(expectedSimpleArb),
          MkHListArbitrary.hconsMkArb(
            Lazy(expectedComposedArb),
            MkHListArbitrary.hconsMkArb(
              Lazy(Arbitrary.arbInt),
              MkHListArbitrary.hnilMkArb,
              ops.hlist.Length[HNil],
              ops.nat.ToInt[Nat._0]
            ),
            ops.hlist.Length[Int :: HNil],
            ops.nat.ToInt[Nat._1]
          ),
          ops.hlist.Length[Composed :: Int :: HNil],
          ops.nat.ToInt[Nat._2]
        )
      )
    ).arbitrary

  lazy val expectedComposedOptListArb =
    MkArbitrary.genericProductMkArb(
      Generic[ComposedOptList],
      Lazy(
        MkHListArbitrary.hconsMkArb(
          Lazy(Arbitrary.arbOption(expectedSimpleArb)),
          MkHListArbitrary.hconsMkArb(
            Lazy(Arbitrary.arbString),
            MkHListArbitrary.hconsMkArb(
              Lazy(Arbitrary.arbContainer[List, TwiceComposed](expectedTwiceComposedArb, implicitly, identity)),
              MkHListArbitrary.hnilMkArb,
              ops.hlist.Length[HNil],
              ops.nat.ToInt[Nat._0]
            ),
            ops.hlist.Length[List[TwiceComposed] :: HNil],
            ops.nat.ToInt[Nat._1]
          ),
          ops.hlist.Length[String :: List[TwiceComposed] :: HNil],
          ops.nat.ToInt[Nat._2]
        )
      )
    ).arbitrary

  lazy val expectedBaseArb =
    MkArbitrary.genericCoproductMkArb(
      Generic[Base],
      Lazy(
        MkCoproductArbitrary.cconsMkArb(
          Lazy(
            MkArbitrary.genericProductMkArb(
              Generic[BaseDB],
              Lazy(
                MkHListArbitrary.hconsMkArb(
                  Lazy(Arbitrary.arbDouble),
                  MkHListArbitrary.hconsMkArb(
                    Lazy(Arbitrary.arbBool),
                    MkHListArbitrary.hnilMkArb,
                    ops.hlist.Length[HNil],
                    ops.nat.ToInt[Nat._0]
                  ),
                  ops.hlist.Length[Boolean :: HNil],
                  ops.nat.ToInt[Nat._1]
                )
              )
            ).arbitrary
          ),
          MkCoproductArbitrary.cconsMkArb(
            Lazy(
              MkArbitrary.genericProductMkArb(
                Generic[BaseIS],
                Lazy(
                  MkHListArbitrary.hconsMkArb(
                    Lazy(Arbitrary.arbInt),
                    MkHListArbitrary.hconsMkArb(
                      Lazy(Arbitrary.arbString),
                      MkHListArbitrary.hnilMkArb,
                      ops.hlist.Length[HNil],
                      ops.nat.ToInt[Nat._0]
                    ),
                    ops.hlist.Length[String :: HNil],
                    ops.nat.ToInt[Nat._1]
                  )
                )
              ).arbitrary
            ),
            MkCoproductArbitrary.cconsMkArb(
              Lazy(
                MkArbitrary.genericProductMkArb(
                  Generic[BaseLast],
                  Lazy(
                    MkHListArbitrary.hconsMkArb(
                      Lazy(expectedSimpleArb),
                      MkHListArbitrary.hnilMkArb,
                      ops.hlist.Length[HNil],
                      ops.nat.ToInt[Nat._0]
                    )
                  )
                ).arbitrary
              ),
              MkCoproductArbitrary.cnilMkArb,
              ops.coproduct.Length[CNil],
              ops.nat.ToInt[Nat._0]
            ),
            ops.coproduct.Length[BaseLast :+: CNil],
            ops.nat.ToInt[Nat._1]
          ),
          ops.coproduct.Length[BaseIS :+: BaseLast :+: CNil],
          ops.nat.ToInt[Nat._2]
        )
      )
    ).arbitrary

  lazy val expectedCCWithSingletonArb =
    MkArbitrary.genericProductMkArb(
      Generic[CCWithSingleton],
      Lazy(
        MkHListArbitrary.hconsMkArb(
          Lazy(Arbitrary.arbInt),
          MkHListArbitrary.hconsMkArb(
            Lazy(Shapeless.arbitrarySingletonType[Witness.`"aa"`.T]),
            MkHListArbitrary.hnilMkArb,
            ops.hlist.Length[HNil],
            ops.nat.ToInt[Nat._0]
          ),
          ops.hlist.Length[Witness.`"aa"`.T :: HNil],
          ops.nat.ToInt[Nat._1]
        )
      )
    ).arbitrary

  lazy val expectedBaseWithSingletonMainArb =
    MkArbitrary.genericProductMkArb(
      Generic[BaseWithSingleton.Main],
      Lazy(
        MkHListArbitrary.hconsMkArb(
          Lazy(Shapeless.arbitrarySingletonType[Witness.`"aa"`.T]),
          MkHListArbitrary.hnilMkArb,
          ops.hlist.Length[HNil],
          ops.nat.ToInt[Nat._0]
        )
      )
    ).arbitrary

  lazy val expectedBaseWithSingletonDummyArb =
    MkArbitrary.genericProductMkArb(
      Generic[BaseWithSingleton.Dummy],
      Lazy(
        MkHListArbitrary.hconsMkArb(
          Lazy(Arbitrary.arbInt),
          MkHListArbitrary.hnilMkArb,
          ops.hlist.Length[HNil],
          ops.nat.ToInt[Nat._0]
        )
      )
    ).arbitrary

  lazy val expectedBaseWithSingletonArb =
    MkArbitrary.genericCoproductMkArb(
      Generic[BaseWithSingleton],
      Lazy(
        MkCoproductArbitrary.cconsMkArb(
          Lazy(
            expectedBaseWithSingletonDummyArb
          ),
          MkCoproductArbitrary.cconsMkArb(
            Lazy(
              expectedBaseWithSingletonMainArb
            ),
            MkCoproductArbitrary.cnilMkArb,
            ops.coproduct.Length[CNil],
            ops.nat.ToInt[Nat._0]
          ),
          ops.coproduct.Length[BaseWithSingleton.Main :+: CNil],
          ops.nat.ToInt[Nat._1]
        )
      )
    ).arbitrary

  lazy val expectedT1TreeArbitrary: Arbitrary[T1.Tree] =
    MkArbitrary.genericCoproductMkArb(
      Generic[T1.Tree],
      Lazy(
        MkCoproductArbitrary.cconsMkArb(
          Lazy(
            MkArbitrary.genericProductMkArb(
              Generic[T1.Leaf.type],
              Lazy(
                MkHListArbitrary.hnilMkArb
              )
            ).arbitrary
          ),
          MkCoproductArbitrary.cconsMkArb(
            Lazy(
              MkArbitrary.genericProductMkArb(
                Generic[T1.Node],
                Lazy(
                  MkHListArbitrary.hconsMkArb(
                    Lazy(
                      expectedT1TreeArbitrary
                    ),
                    MkHListArbitrary.hconsMkArb(
                      Lazy(
                        expectedT1TreeArbitrary
                      ),
                      MkHListArbitrary.hconsMkArb(
                        Lazy(
                          Arbitrary.arbInt
                        ),
                        MkHListArbitrary.hnilMkArb,
                        ops.hlist.Length[HNil],
                        ops.nat.ToInt[Nat._0]
                      ),
                      ops.hlist.Length[Int :: HNil],
                      ops.nat.ToInt[Nat._1]
                    ),
                    ops.hlist.Length[T1.Tree :: Int :: HNil],
                    ops.nat.ToInt[Nat._2]
                  )
                )
              ).arbitrary
            ),
            MkCoproductArbitrary.cnilMkArb,
            ops.coproduct.Length[CNil],
            ops.nat.ToInt[Nat._0]
          ),
          ops.coproduct.Length[T1.Node :+: CNil],
          ops.nat.ToInt[Nat._1]
        )
      )
    ).arbitrary

  lazy val expectedT2TreeArbitrary: Arbitrary[T2.Tree] =
    MkArbitrary.genericCoproductMkArb(
      Generic[T2.Tree],
      Lazy(
        MkCoproductArbitrary.cconsMkArb(
          Lazy(
            MkArbitrary.genericProductMkArb(
              Generic[T2.Leaf.type],
              Lazy(
                MkHListArbitrary.hnilMkArb
              )
            ).arbitrary
          ),
          MkCoproductArbitrary.cconsMkArb(
            Lazy(
              MkArbitrary.genericProductMkArb(
                Generic[T2.Node],
                Lazy(
                  MkHListArbitrary.hconsMkArb(
                    Lazy(
                      expectedT2TreeArbitrary
                    ),
                    MkHListArbitrary.hconsMkArb(
                      Lazy(
                        expectedT2TreeArbitrary
                      ),
                      MkHListArbitrary.hconsMkArb(
                        Lazy(
                          Arbitrary.arbInt
                        ),
                        MkHListArbitrary.hnilMkArb,
                        ops.hlist.Length[HNil],
                        ops.nat.ToInt[Nat._0]
                      ),
                      ops.hlist.Length[Int :: HNil],
                      ops.nat.ToInt[Nat._1]
                    ),
                    ops.hlist.Length[T2.Tree :: Int :: HNil],
                    ops.nat.ToInt[Nat._2]
                  )
                )
              ).arbitrary
            ),
            MkCoproductArbitrary.cnilMkArb,
            ops.coproduct.Length[CNil],
            ops.nat.ToInt[Nat._0]
          ),
          ops.coproduct.Length[T2.Node :+: CNil],
          ops.nat.ToInt[Nat._1]
        )
      )
    ).arbitrary

  lazy val expectedBazArbitrary =
    MkArbitrary.genericProductMkArb(
      Generic[Baz.type],
      Lazy(
        MkHListArbitrary.hnilMkArb
      )
    ).arbitrary

  lazy val expectedFooArbitrary =
    MkArbitrary.genericCoproductMkArb(
      Generic[Foo],
      Lazy(
        MkCoproductArbitrary.cconsMkArb(
          Lazy(
            expectedBazArbitrary
          ),
          MkCoproductArbitrary.cnilMkArb,
          ops.coproduct.Length[CNil],
          ops.nat.ToInt[Nat._0]
        )
      )
    ).arbitrary

  lazy val expectedFArbitrary =
    MkArbitrary.genericProductMkArb(
      Generic[F.type],
      Lazy(
        MkHListArbitrary.hnilMkArb
      )
    ).arbitrary

  lazy val expectedEArbitrary =
    MkArbitrary.genericProductMkArb(
      Generic[E],
      Lazy(
        MkHListArbitrary.hconsMkArb(
          Lazy(
            Arbitrary.arbDouble
          ),
          MkHListArbitrary.hconsMkArb(
            Lazy(
              Arbitrary.arbOption(Arbitrary.arbFloat)
            ),
            MkHListArbitrary.hnilMkArb,
            ops.hlist.Length[HNil],
            ops.nat.ToInt[Nat._0]
          ),
          ops.hlist.Length[Option[Float] :: HNil],
          ops.nat.ToInt[Nat._1]
        )
      )
    ).arbitrary

  lazy val expectedDArbitrary =
    MkArbitrary.genericCoproductMkArb(
      Generic[D],
      Lazy(
        MkCoproductArbitrary.cconsMkArb(
          Lazy(
            expectedBazArbitrary
          ),
          MkCoproductArbitrary.cconsMkArb(
            Lazy(
              expectedEArbitrary
            ),
            MkCoproductArbitrary.cconsMkArb(
              Lazy(
                expectedFArbitrary
              ),
              MkCoproductArbitrary.cnilMkArb,
              ops.coproduct.Length[CNil],
              ops.nat.ToInt[Nat._0]
            ),
            ops.coproduct.Length[F.type :+: CNil],
            ops.nat.ToInt[Nat._1]
          ),
          ops.coproduct.Length[E :+: F.type :+: CNil],
          ops.nat.ToInt[Nat._2]
        )
      )
    ).arbitrary

  lazy val expectedCArbitrary =
    MkArbitrary.genericProductMkArb(
      Generic[C.type],
      Lazy(
        MkHListArbitrary.hnilMkArb
      )
    ).arbitrary

  lazy val expectedBArbitrary =
    MkArbitrary.genericProductMkArb(
      Generic[B],
      Lazy(
        MkHListArbitrary.hconsMkArb(
          Lazy(
            Arbitrary.arbInt
          ),
          MkHListArbitrary.hconsMkArb(
            Lazy(
              Arbitrary.arbString
            ),
            MkHListArbitrary.hnilMkArb,
            ops.hlist.Length[HNil],
            ops.nat.ToInt[Nat._0]
          ),
          ops.hlist.Length[String :: HNil],
          ops.nat.ToInt[Nat._1]
        )
      )
    ).arbitrary

  lazy val expectedAArbitrary =
    MkArbitrary.genericCoproductMkArb(
      Generic[A],
      Lazy(
        MkCoproductArbitrary.cconsMkArb(
          Lazy(
            expectedBArbitrary
          ),
          MkCoproductArbitrary.cconsMkArb(
            Lazy(
              expectedBazArbitrary
            ),
            MkCoproductArbitrary.cconsMkArb(
              Lazy(
                expectedCArbitrary
              ),
              MkCoproductArbitrary.cconsMkArb(
                Lazy(
                  expectedEArbitrary
                ),
                MkCoproductArbitrary.cconsMkArb(
                  Lazy(
                    expectedFArbitrary
                  ),
                  MkCoproductArbitrary.cnilMkArb,
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
      )
    ).arbitrary


  val tests = TestSuite {

    'compareSuccess - {
      val arb = Arbitrary.arbInt.arbitrary
      compare(arb, arb)
    }

    'compareFailure - {
      val arb = Arbitrary.arbInt
      val result =
        try {
          compare(arb.arbitrary, arb.arbitrary.map(_ + 1))
          false
        }
        catch {
          case _: utest.AssertionError => true
        }

      assert(result)
    }

    'empty - {
      val expectedArb =
        MkArbitrary.genericProductMkArb(
          Generic[Empty.type],
          Lazy(MkHListArbitrary.hnilMkArb)
        ).arbitrary

      val gen = Arbitrary.arbitrary[Empty.type]
      compare(expectedArb.arbitrary, gen)
    }

    'emptyCC - {
      val expectedArb =
        MkArbitrary.genericProductMkArb(
          Generic[EmptyCC],
          Lazy(MkHListArbitrary.hnilMkArb)
        ).arbitrary

      val gen = Arbitrary.arbitrary[EmptyCC]
      compare(expectedArb.arbitrary, gen)
    }

    'simple - {
      val gen = Arbitrary.arbitrary[Simple]
      compare(expectedSimpleArb.arbitrary, gen)
    }

    'simpleHList - {
      val gen = Arbitrary.arbitrary[Int :: String :: Boolean :: HNil]
      compare(expectedIntStringBoolArb.arbitrary, gen)
    }

    'simpleCoproduct - {
      val gen = Arbitrary.arbitrary[Int :+: String :+: Boolean :+: CNil]
      compare(expectedIntStringBoolCoproductArb.arbitrary, gen)
    }

    'composed - {
      val gen = Arbitrary.arbitrary[Composed]
      compare(expectedComposedArb.arbitrary, gen)
    }

    'twiceComposed - {
      val gen = Arbitrary.arbitrary[TwiceComposed]
      compare(expectedTwiceComposedArb.arbitrary, gen)
    }

    'composedOptList - {
      // Workaround for some Lazy / Priority crash
      implicit val simpleArb = implicitly[Arbitrary[Simple]]

      val gen = Arbitrary.arbitrary[ComposedOptList]
      compare(expectedComposedOptListArb.arbitrary, gen)
    }

    'base - {
      val gen = Arbitrary.arbitrary[Base]
      compare(expectedBaseArb.arbitrary, gen)
    }

    'tree1 - {
      val gen = Arbitrary.arbitrary[T1.Tree]
      compare(expectedT1TreeArbitrary.arbitrary, gen)
    }

    'tree2 - {
      val gen = Arbitrary.arbitrary[T2.Tree]
      compare(expectedT2TreeArbitrary.arbitrary, gen)
    }

    'a - {
      val gen = Arbitrary.arbitrary[A]
      compare(expectedAArbitrary.arbitrary, gen)
    }

    'd - {
      val gen = Arbitrary.arbitrary[D]
      compare(expectedDArbitrary.arbitrary, gen)
    }

    'list - {
      val expected = Arbitrary.arbContainer[List, Int](Arbitrary.arbInt, implicitly, identity)
      val gen = Arbitrary.arbitrary[List[Int]]
      compare(expected.arbitrary, gen)
    }

    'option - {
      val expected = Arbitrary.arbOption(Arbitrary.arbInt)
      val gen = Arbitrary.arbitrary[Option[Int]]
      compare(expected.arbitrary, gen)
    }

    'singleton - {
      'simple - {
        val expected = Arbitrary(Gen.const(2: Witness.`2`.T))
        val gen = Arbitrary.arbitrary[Witness.`2`.T]
        compare(expected.arbitrary, gen)
      }

      'caseClass - {
        val gen = Arbitrary.arbitrary[CCWithSingleton]
        compare(expectedCCWithSingletonArb.arbitrary, gen)
      }

      'ADT - {
        val gen = Arbitrary.arbitrary[BaseWithSingleton]
        compare(expectedBaseWithSingletonArb.arbitrary, gen)
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
