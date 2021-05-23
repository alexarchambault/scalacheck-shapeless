package org.scalacheck

import org.scalacheck.derive._

import shapeless.{test => _, _}
import shapeless.labelled.FieldType
import shapeless.record.Record
import shapeless.union.Union

import utest._

import Util._

object CogenTests extends TestSuite {
  import TestsDefinitions._
  import ScalacheckShapeless._

  lazy val expectedIntStringBoolCogen =
    expectedIntStringBoolMkHListCogen.cogen
  lazy val expectedIntStringBoolMkHListCogen =
    MkHListCogen.hcons(
      Cogen.cogenInt,
      MkHListCogen.hcons(
        Cogen.cogenString,
        MkHListCogen.hcons(
          Cogen.cogenBoolean,
          MkHListCogen.hnil
        )
      )
    )

  lazy val expectedIntStringBoolCoproductCogen =
    MkCoproductCogen.ccons(
      Cogen.cogenInt,
      MkCoproductCogen.ccons(
        Cogen.cogenString,
        MkCoproductCogen.ccons(
          Cogen.cogenBoolean,
          MkCoproductCogen.cnil
        )
      )
    ).cogen

  lazy val expectedSimpleCogen =
    MkCogen.genericProduct(
      Generic[Simple],
      expectedIntStringBoolMkHListCogen
    ).cogen

  lazy val expectedRecCogen =
    MkHListCogen.hcons[FieldType[Witness.`'i`.T, Int], Record.`'s -> String`.T](
      cogenFieldType[Witness.`'i`.T, Int](Cogen.cogenInt),
      MkHListCogen.hcons[FieldType[Witness.`'s`.T, String], HNil](
        cogenFieldType[Witness.`'s`.T, String](Cogen.cogenString),
        MkHListCogen.hnil
      )
    ).cogen

  lazy val expectedUnionCogen =
    MkCoproductCogen.ccons[FieldType[Witness.`'i`.T, Int], Union.`'s -> String`.T](
      cogenFieldType[Witness.`'i`.T, Int](Cogen.cogenInt),
      MkCoproductCogen.ccons[FieldType[Witness.`'s`.T, String], CNil](
        cogenFieldType[Witness.`'s`.T, String](Cogen.cogenString),
        MkCoproductCogen.cnil
      )
    ).cogen

  lazy val expectedTree1Cogen: Cogen[T1.Tree] =
    MkCogen.genericCoproduct(
      Generic[T1.Tree],
      Lazy(MkCoproductCogen.ccons(
        MkCogen.genericProduct(
          Generic[T1.Leaf.type],
          Lazy(MkHListCogen.hnil)
        ).cogen,
        MkCoproductCogen.ccons(
          MkCogen.genericProduct(
            Generic[T1.Node],
            Lazy(MkHListCogen.hcons(
              expectedTree1Cogen,
              MkHListCogen.hcons(
                expectedTree1Cogen,
                MkHListCogen.hcons(
                  Cogen.cogenInt,
                  MkHListCogen.hnil
                ))))
          ).cogen,
          MkCoproductCogen.cnil
        )))
    ).cogen

  val tests = TestSuite {

    test("compareSuccess") {
      val cogen = Cogen.cogenInt
      compareCogen(cogen, cogen)
    }

    test("compareFailure") {
      val cogen = Cogen.cogenInt
      val result =
        try {
          compareCogen(cogen, cogen.contramap[Int](_ + 1))
          false
        }
        catch {
          case _: java.lang.AssertionError => true
        }

      assert(result)
    }

    test("empty") {
      val expectedCogen =
        MkCogen.genericProduct(
          Generic[Empty.type],
          Lazy(MkHListCogen.hnil)
        ).cogen

      val cogen = Cogen[Empty.type]
      compareCogen(expectedCogen, cogen)
    }

    test("emptyAsSingleton") {
      val expectedCogen =
        cogenSingletonType[Empty.type]

      val cogen = Cogen[Empty.type]
      compareCogen(expectedCogen, cogen)
    }

    test("emptyCC") {
      val expectedCogen =
        MkCogen.genericProduct(
          Generic[EmptyCC],
          Lazy(MkHListCogen.hnil)
        ).cogen

      val cogen = Cogen[EmptyCC]
      compareCogen(expectedCogen, cogen)
    }

    test("simple") {
      val cogen = Cogen[Simple]
      compareCogen(expectedSimpleCogen, cogen)
    }

    test("simpleHList") {
      val cogen = Cogen[Int :: String :: Boolean :: HNil]
      compareCogen(expectedIntStringBoolCogen, cogen)
    }

    test("simpleCoproduct") {
      val cogen = Cogen[Int :+: String :+: Boolean :+: CNil]
      compareCogen(expectedIntStringBoolCoproductCogen, cogen)
    }

    test("simpleRec") {
      val cogen = Cogen[Rec]
      compareCogen(expectedRecCogen, cogen)
    }

    test("simpleUnion") {
      val cogen = Cogen[Un]
      compareCogen(expectedUnionCogen, cogen)
    }

    test("tree1") {
      val cogen = Cogen[T1.Tree]
      compareCogen(expectedTree1Cogen, cogen)
    }
  }

}
