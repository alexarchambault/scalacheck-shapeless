package org.scalacheck

import org.scalacheck.TestsDefinitions._
import org.scalacheck.derive.{MkHListCogen, MkCogen}
import org.scalacheck.rng.{Rng, Seed}
import shapeless._
import utest._

object CogenTests extends TestSuite {
  import Shapeless._

  lazy val expectedSimpleCogen =
    MkCogen.genericProductCogen(
      Generic[Simple],
      Lazy(
        MkHListCogen.hconsCogen(
          Lazy(Cogen.cogenInt),
          Lazy(
            MkHListCogen.hconsCogen(
              Lazy(Cogen.cogenString),
              Lazy(
                MkHListCogen.hconsCogen(
                  Lazy(Cogen.cogenBoolean),
                  Lazy(
                    MkHListCogen.hnilCogen
                  )
                )
              )
            )
          )
        )
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
      .map(Rng.long)
      .map(_._1)
    val secondSeeds = values.scanLeft(seed)(second.perturb)
      .map(Rng.long)
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
        MkCogen.genericProductCogen(
          Generic[Empty.type],
          Lazy(MkHListCogen.hnilCogen)
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
        MkCogen.genericProductCogen(
          Generic[EmptyCC],
          Lazy(MkHListCogen.hnilCogen)
        ).cogen

      val cogen = Cogen[EmptyCC]
      compare(expectedCogen, cogen)
    }

    'simple - {
      val cogen = Cogen[Simple]
      compare(expectedSimpleCogen, cogen)
    }

  }

}
