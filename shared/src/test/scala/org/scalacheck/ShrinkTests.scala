package org.scalacheck

import org.scalacheck.derive.{MkHListShrink, MkShrink}
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
    MkShrink.genericProductShrink(
      Generic[Simple],
      Lazy(
        MkHListShrink.hconsShrink(
          Lazy(Shrink.shrinkInt),
          Lazy(
            MkHListShrink.hconsShrink(
              Lazy(Shrink.shrinkString),
              Lazy(
                MkHListShrink.hconsShrink(
                  Lazy(Shrink.shrinkAny[Boolean]),
                  Lazy(
                    MkHListShrink.hnilShrink
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
