package org.scalacheck

import org.scalacheck.derive._
import shapeless.{ Lazy => _, _ }
import shapeless.compat._
import shapeless.labelled._
import shapeless.record.Record
import shapeless.union.Union

import utest._
import Util._

import Shapeless._

object ShrinkTests extends TestSuite {
  import TestsDefinitions._

  lazy val expectedListIntShrink =
    Shrink.shrinkContainer[List, Int](identity, Shrink.shrinkIntegral[Int], implicitly)

  lazy val expectedOptionIntShrink =
    Shrink.shrinkOption(Shrink.shrinkIntegral[Int])

  lazy val expectedIntStringBoolShrink =
    expectedIntStringBoolMkHListShrink.shrink
  lazy val expectedIntStringBoolMkHListShrink =
    MkHListShrink.hconsShrink(
      Strict(Shrink.shrinkIntegral[Int]),
      MkHListShrink.hconsShrink(
        Strict(Shrink.shrinkString),
        MkHListShrink.hconsShrink(
          Strict(Shrink.shrinkAny[Boolean]),
          MkHListShrink.hnilShrink
        )
      )
    )

  lazy val expectedIntStringBoolCoproductShrink =
    MkCoproductShrink.cconsShrink(
      Strict(Shrink.shrinkIntegral[Int]),
      MkCoproductShrink.cconsShrink(
        Strict(Shrink.shrinkString),
        MkCoproductShrink.cconsShrink(
          Strict(Shrink.shrinkAny[Boolean]),
          MkCoproductShrink.cnilShrink,
          Strict(Singletons[Boolean]),
          Strict(Singletons[CNil])
        ),
        Strict(Singletons[String]),
        Strict(Singletons[Boolean :+: CNil])
      ),
      Strict(Singletons[Int]),
      Strict(Singletons[String :+: Boolean :+: CNil])
    ).shrink

  lazy val expectedSimpleShrink =
    MkShrink.genericProductShrink(
      Generic[Simple],
      Lazy(
        expectedIntStringBoolMkHListShrink
      )
    ).shrink

  lazy val expectedRecShrink =
    MkHListShrink.hconsShrink[FieldType[Witness.`'i`.T, Int], Record.`'s -> String`.T](
      shrinkFieldType[Witness.`'i`.T, Int](Shrink.shrinkIntegral[Int]),
      MkHListShrink.hconsShrink[FieldType[Witness.`'s`.T, String], HNil](
        shrinkFieldType[Witness.`'s`.T, String](Shrink.shrinkString),
        MkHListShrink.hnilShrink
      )
    ).shrink

  lazy val expectedUnionShrink =
    MkCoproductShrink.cconsShrink[FieldType[Witness.`'i`.T, Int], Union.`'s -> String`.T](
      shrinkFieldType[Witness.`'i`.T, Int](Shrink.shrinkIntegral[Int]),
      MkCoproductShrink.cconsShrink[FieldType[Witness.`'s`.T, String], CNil](
        shrinkFieldType[Witness.`'s`.T, String](Shrink.shrinkString),
        MkCoproductShrink.cnilShrink,
        Singletons[FieldType[Witness.`'s`.T, String]],
        Singletons[CNil]
      ),
      Singletons[FieldType[Witness.`'i`.T, Int]],
      Singletons[Union.`'s -> String`.T]
    ).shrink


  def compare[T: Arbitrary](first: Shrink[T], second: Shrink[T]): Unit =
    Prop.forAll {
      t: T =>
        first.shrink(t) == second.shrink(t)
    }.validate

  val tests = TestSuite {

    'listInt - {
      val shrink = implicitly[Shrink[List[Int]]]
      compare(shrink, expectedListIntShrink)
    }

    'optionInt - {
      val shrink = implicitly[Shrink[Option[Int]]]
      compare(shrink, expectedOptionIntShrink)
    }

    'simple - {
       val shrink = implicitly[Shrink[Simple]]
       compare(shrink, expectedSimpleShrink)
    }

    'simpleHList - {
      val shrink = implicitly[Shrink[Int :: String :: Boolean :: HNil]]
      compare(shrink, expectedIntStringBoolShrink)
    }

    'simpleCoproduct - {
      val shrink = implicitly[Shrink[Int :+: String :+: Boolean :+: CNil]]
      compare(shrink, expectedIntStringBoolCoproductShrink)
    }

    'simpleRecord - {
      val shrink = implicitly[Shrink[Rec]]
      compare(shrink, expectedRecShrink)
    }

    'simpleUnion - {
      val shrink = implicitly[Shrink[Un]]
      compare(shrink, expectedUnionShrink)
    }

  }

}
