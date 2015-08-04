package org.scalacheck

import org.scalacheck.derive.MkShrink
import shapeless._
import utest._
import Util._

import Shapeless._

object ShrinkTests extends TestSuite {
  import TestsDefinitions._

  lazy val expectedListIntShrink =
    Shrink.shrinkContainer[List, Int](identity, Shrink.shrinkInt, implicitly)

  lazy val expectedOptionIntShrink =
    Shrink.shrinkOption(Shrink.shrinkInt)

  lazy val expectedSimpleShrink =
    MkShrink.genericShrink(
      Generic[Simple],
      Lazy(
        MkShrink.hconsShrink(
          Lazy(Shrink.shrinkInt),
          Lazy(
            MkShrink.hconsShrink(
              Lazy(Shrink.shrinkString),
              Lazy(
                MkShrink.hconsShrink(
                  Lazy(Shrink.shrinkAny[Boolean]),
                  Lazy(
                    MkShrink.hnilShrink
                  )
                )
              )
            )
          )
        )
      )
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

  }

}
