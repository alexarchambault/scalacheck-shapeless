package org.scalacheck

import org.scalacheck.Shapeless._
import org.scalacheck.derive._

import shapeless.{ Lazy => _, _ }
import shapeless.compat._
import shapeless.labelled._
import shapeless.record.Record
import shapeless.union.Union

import utest._

import Util._

object ShrinkTests extends TestSuite {
  import TestsDefinitions._

  lazy val expectedListIntShrink =
    Shrink.shrinkContainer[List, Int](identity, Shrink.shrinkIntegral[Int], implicitly)

  lazy val expectedOptionIntShrink =
    Shrink.shrinkOption(Shrink.shrinkIntegral[Int])

  lazy val expectedIntStringBoolShrink =
    expectedIntStringBoolMkHListShrink.shrink
  lazy val expectedIntStringBoolMkHListShrink =
    MkHListShrink.hcons(
      Strict(Shrink.shrinkIntegral[Int]),
      MkHListShrink.hcons(
        Strict(Shrink.shrinkString),
        MkHListShrink.hcons(
          Strict(Shrink.shrinkAny[Boolean]),
          MkHListShrink.hnil
        )
      )
    )

  lazy val expectedIntStringBoolCoproductShrink =
    MkCoproductShrink.ccons(
      Strict(Shrink.shrinkIntegral[Int]),
      MkCoproductShrink.ccons(
        Strict(Shrink.shrinkString),
        MkCoproductShrink.ccons(
          Strict(Shrink.shrinkAny[Boolean]),
          MkCoproductShrink.cnil,
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
    MkShrink.genericProduct(
      Generic[Simple],
      Lazy(
        expectedIntStringBoolMkHListShrink
      )
    ).shrink

  lazy val expectedRecShrink =
    MkHListShrink.hcons[FieldType[Witness.`'i`.T, Int], Record.`'s -> String`.T](
      shrinkFieldType[Witness.`'i`.T, Int](Shrink.shrinkIntegral[Int]),
      MkHListShrink.hcons[FieldType[Witness.`'s`.T, String], HNil](
        shrinkFieldType[Witness.`'s`.T, String](Shrink.shrinkString),
        MkHListShrink.hnil
      )
    ).shrink

  lazy val expectedUnionShrink =
    MkCoproductShrink.ccons[FieldType[Witness.`'i`.T, Int], Union.`'s -> String`.T](
      shrinkFieldType[Witness.`'i`.T, Int](Shrink.shrinkIntegral[Int]),
      MkCoproductShrink.ccons[FieldType[Witness.`'s`.T, String], CNil](
        shrinkFieldType[Witness.`'s`.T, String](Shrink.shrinkString),
        MkCoproductShrink.cnil,
        Singletons[FieldType[Witness.`'s`.T, String]],
        Singletons[CNil]
      ),
      Singletons[FieldType[Witness.`'i`.T, Int]],
      Singletons[Union.`'s -> String`.T]
    ).shrink


  val tests = TestSuite {

    'listInt - {
      val shrink = implicitly[Shrink[List[Int]]]
      compareShrink(shrink, expectedListIntShrink)
    }

    'optionInt - {
      val shrink = implicitly[Shrink[Option[Int]]]
      compareShrink(shrink, expectedOptionIntShrink)
    }

    'simple - {
       val shrink = implicitly[Shrink[Simple]]
       compareShrink(shrink, expectedSimpleShrink)
    }

    'simpleHList - {
      val shrink = implicitly[Shrink[Int :: String :: Boolean :: HNil]]
      compareShrink(shrink, expectedIntStringBoolShrink)
    }

    'simpleCoproduct - {
      val shrink = implicitly[Shrink[Int :+: String :+: Boolean :+: CNil]]
      compareShrink(shrink, expectedIntStringBoolCoproductShrink)
    }

    'simpleRecord - {
      val shrink = implicitly[Shrink[Rec]]
      compareShrink(shrink, expectedRecShrink)
    }

    'simpleUnion - {
      val shrink = implicitly[Shrink[Un]]
      compareShrink(shrink, expectedUnionShrink)
    }

  }

}
