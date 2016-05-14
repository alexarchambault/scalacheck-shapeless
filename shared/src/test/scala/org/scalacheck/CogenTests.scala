package org.scalacheck

import org.scalacheck.TestsDefinitions._
import org.scalacheck.derive._
import org.scalacheck.rng.Seed
import shapeless.{ Lazy => _, _ }
import shapeless.compat._
import shapeless.labelled.FieldType
import shapeless.record.Record
import shapeless.union.Union
import utest._

object CogenTests extends TestSuite {
  import Shapeless._

  lazy val expectedIntStringBoolCogen =
    expectedIntStringBoolMkHListCogen.cogen
  lazy val expectedIntStringBoolMkHListCogen =
    MkHListCogen.hcons(
      Strict(Cogen.cogenInt),
      MkHListCogen.hcons(
        Strict(Cogen.cogenString),
        MkHListCogen.hcons(
          Strict(Cogen.cogenBoolean),
          MkHListCogen.hnil
        )
      )
    )

  lazy val expectedIntStringBoolCoproductCogen =
    MkCoproductCogen.ccons(
      Strict(Cogen.cogenInt),
      MkCoproductCogen.ccons(
        Strict(Cogen.cogenString),
        MkCoproductCogen.ccons(
          Strict(Cogen.cogenBoolean),
          MkCoproductCogen.cnil
        )
      )
    ).cogen

  lazy val expectedSimpleCogen =
    MkCogen.genericProduct(
      Generic[Simple],
      Lazy(expectedIntStringBoolMkHListCogen)
    ).cogen

  lazy val expectetRecCogen =
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


  def stream[T](gen: Gen[T]): Stream[Option[T]] = {
    val elem = gen.sample
    elem #:: stream(gen)
  }

  def doCompare[T: Arbitrary](
    seed: Seed )(
    first: Cogen[T],
    second: Cogen[T] )(
    len: Int,
    min: Int
  ): Unit = {
    val values = stream(Arbitrary.arbitrary[T])
      .take(len)
      .collect{case Some(t) => t }

    assert(values.lengthCompare(min) >= 0)

    val firstSeeds = values.scanLeft(seed)(first.perturb)
      .map(_.long)
      .map(_._1)
    val secondSeeds = values.scanLeft(seed)(second.perturb)
      .map(_.long)
      .map(_._1)
    val seeds = firstSeeds zip secondSeeds

    assert(seeds.forall{case (a, b) => a == b})
  }

  def compare[T: Arbitrary](first: Cogen[T], second: Cogen[T]): Unit =
    doCompare(Seed.random())(first, second)(160, 40)

  val tests = TestSuite {

    'compareSuccess - {
      val cogen = Cogen.cogenInt
      compare(cogen, cogen)
    }

    'compareFailure - {
      val cogen = Cogen.cogenInt
      val result =
        try {
          compare(cogen, cogen.contramap[Int](_ + 1))
          false
        }
        catch {
          case _: utest.AssertionError => true
        }

      assert(result)
    }

    'empty - {
      val expectedCogen =
        MkCogen.genericProduct(
          Generic[Empty.type],
          Lazy(MkHListCogen.hnil)
        ).cogen

      val cogen = Cogen[Empty.type]
      compare(expectedCogen, cogen)
    }

    'emptyAsSingleton - {
      val expectedCogen =
        cogenSingletonType[Empty.type]

      val cogen = Cogen[Empty.type]
      compare(expectedCogen, cogen)
    }

    'emptyCC - {
      val expectedCogen =
        MkCogen.genericProduct(
          Generic[EmptyCC],
          Lazy(MkHListCogen.hnil)
        ).cogen

      val cogen = Cogen[EmptyCC]
      compare(expectedCogen, cogen)
    }

    'simple - {
      val cogen = Cogen[Simple]
      compare(expectedSimpleCogen, cogen)
    }

    'simpleHList - {
      val cogen = Cogen[Int :: String :: Boolean :: HNil]
      compare(expectedIntStringBoolCogen, cogen)
    }

    'simpleCoproduct - {
      val cogen = Cogen[Int :+: String :+: Boolean :+: CNil]
      compare(expectedIntStringBoolCoproductCogen, cogen)
    }

    'simpleRec - {
      val cogen = Cogen[Rec]
      compare(expectetRecCogen, cogen)
    }

    'simpleUnion - {
      val cogen = Cogen[Un]
      compare(expectedUnionCogen, cogen)
    }

  }

}
