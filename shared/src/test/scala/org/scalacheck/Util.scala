package org.scalacheck

import org.scalacheck.Gen.Parameters
import org.scalacheck.rng.Seed

import utest._

object Util {

  implicit class PropExtensions(val prop: Prop) extends AnyVal {
    def validate: Unit = {
      val result = Test.check(Test.Parameters.default, prop)
      assert(result.passed)
    }
  }

  def streamOf[T](parameters: Parameters, seed: Seed)(arbitrary: Gen[T]): Stream[Option[T]] = {
    def helper(seed: Seed): Stream[Option[T]] = {
      val r = arbitrary.doApply(parameters, seed)
      r.retrieve #:: helper(r.seed)
    }

    helper(seed)
  }

  def compareArbitraryHelper[T](
    parameters: Parameters,
    seed: Seed )(
    first: Gen[T],
    second: Gen[T] )(
    len: Int
  ): Unit = {
    val generated =
      streamOf(parameters, seed)(first)
        .zip(streamOf(parameters, seed)(second))
        .take(len)

    assert(generated.forall{case (a, b) => a == b})
  }

  /** Ask each `Gen[T]` a sequence of values, given the same parameters and initial seed,
    * and throw an exception if both sequences aren't equal. */
  def compareArbitrary[T](first: Gen[T], second: Gen[T]): Unit =
    compareArbitraryHelper(Parameters.default, Seed.random())(first, second)(100)


  def streamOf[T](gen: Gen[T]): Stream[Option[T]] = {
    val elem = gen.sample
    elem #:: streamOf(gen)
  }

  def compareCogenHelper[T: Arbitrary](
    seed: Seed )(
    first: Cogen[T],
    second: Cogen[T] )(
    len: Int,
    min: Int
  ): Unit = {
    val values = streamOf(Arbitrary.arbitrary[T])
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

  def compareCogen[T: Arbitrary](first: Cogen[T], second: Cogen[T]): Unit =
    compareCogenHelper(Seed.random())(first, second)(160, 40)

  def compareShrink[T: Arbitrary](first: Shrink[T], second: Shrink[T]): Unit =
    Prop.forAll {
      t: T =>
        first.shrink(t) == second.shrink(t)
    }.validate

  def validateSingletons[T: Singletons](expected: T*) = {
    val found = Singletons[T].apply()
    assert(found == expected)
  }
}
