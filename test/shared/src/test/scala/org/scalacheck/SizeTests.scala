package org.scalacheck

import utest._

import org.scalacheck.rng.Seed

object SizeTests0 {
  import org.scalacheck.ScalacheckShapeless._

  import SizeTestsDefinitions._

  val arbTree = Arbitrary(Arbitrary.arbitrary[Tree])

}

object SizeTests extends TestSuite {

  import SizeTestsDefinitions._

  assert(Leaf.depth == 0)
  assert(Branch(Leaf, Leaf).depth == 1)
  assert(Branch(Branch(Leaf, Leaf), Leaf).depth == 2)


  def stream[T: Arbitrary](p: Gen.Parameters, seed: rng.Seed): Stream[Option[T]] = {
    val r = Arbitrary.arbitrary[T].doPureApply(p, seed)
    r.retrieve #:: stream[T](p, r.seed)
  }

  // manually calculated, grows approx. like log(size)
  val maxDepths = Seq(
    10 -> 5,
    100 -> 8,
    300 -> 10
  )

  val tests = TestSuite {
    'tree - {
      val seed = Seed.random()
      val inspect = 10000

      for ((size, expectedMaxDepth) <- maxDepths) {
        val maxDepth = stream[Tree](Gen.Parameters.default.withSize(size), seed)(SizeTests0.arbTree)
          .map(_.get) // the corresponding generator doesn't fail thanks to the implicit derive.Recursive[Tree] in Tree's companion
          .map(_.depth)
          .take(inspect)
          .max

        assert(maxDepth == expectedMaxDepth)
      }
    }
  }

}