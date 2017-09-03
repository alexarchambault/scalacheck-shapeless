package org.scalacheck

import org.scalacheck.ScalacheckShapeless._
import org.scalacheck.derive._
import shapeless._
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
    MkCoproductShrink0.ccons(
      Strict(Shrink.shrinkIntegral[Int]),
      MkCoproductShrink0.ccons(
        Strict(Shrink.shrinkString),
        MkCoproductShrink0.ccons(
          Strict(Shrink.shrinkAny[Boolean]),
          MkCoproductShrink0.cnil,
          Strict(Singletons[Boolean])
        ),
        Strict(Singletons[String])
      ),
      Strict(Singletons[Int])
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
    MkCoproductShrink0.ccons[FieldType[Witness.`'i`.T, Int], Union.`'s -> String`.T](
      shrinkFieldType[Witness.`'i`.T, Int](Shrink.shrinkIntegral[Int]),
      MkCoproductShrink0.ccons[FieldType[Witness.`'s`.T, String], CNil](
        shrinkFieldType[Witness.`'s`.T, String](Shrink.shrinkString),
        MkCoproductShrink0.cnil,
        Singletons[FieldType[Witness.`'s`.T, String]]
      ),
      Singletons[FieldType[Witness.`'i`.T, Int]]
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

    'adt - {
      val shrink = implicitly[Shrink[A]]

      'caseClass - {
        * - {
          val res = shrink.shrink(B(2, "b")).toVector
          assert(res.contains(C))
          assert(res.contains(F))
          assert(res.contains(Baz))
        }

        * - {
          val res = shrink.shrink(E(1.3, Some(1.2f))).toVector
          assert(res.contains(C))
          assert(res.contains(F))
          assert(res.contains(Baz))
        }
      }

      'caseObject - {
        * - {
          val res = shrink.shrink(C).toVector
          assert(res.isEmpty)
        }

        * - {
          val res = shrink.shrink(F).toVector
          assert(res.isEmpty)
        }

        * - {
          val res = shrink.shrink(Baz).toVector
          assert(res.isEmpty)
        }
      }
    }

    'maybe {
      val shrink = implicitly[Shrink[Maybe]]
      assert(shrink.shrink(Yes).isEmpty)
      assert(shrink.shrink(No).isEmpty)
    }
  }

}
