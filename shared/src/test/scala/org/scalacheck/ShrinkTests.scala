package org.scalacheck

import org.scalacheck.derive.MkDefaultShrink
import shapeless._
import utest._
import Util._

import Shapeless._

object ShrinkTestsDefinitions extends CommonDefinitions

object ShrinkTests extends TestSuite {
  import ShrinkTestsDefinitions._

  lazy val expectedListIntShrink =
    Shrink.shrinkContainer[List, Int](identity, Shrink.shrinkInt, implicitly)

  lazy val expectedOptionIntShrink =
    Shrink.shrinkOption(Shrink.shrinkInt)

  lazy val expectedSimpleShrink =
    MkDefaultShrink.genericShrink(
      Generic[Simple],
      Lazy(
        MkDefaultShrink.hconsShrink(
          Lazy(Shrink.shrinkInt),
          Lazy(
            MkDefaultShrink.hconsShrink(
              Lazy(Shrink.shrinkString),
              Lazy(
                MkDefaultShrink.hconsShrink(
                  Lazy(Shrink.shrinkAny[Boolean]),
                  Lazy(
                    MkDefaultShrink.hnilShrink
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

    // Doesn't pass yet, because of the fallback shrinkAny in org.scalacheck.Shrink
    // 'simple - {
    //   val shrink = implicitly[Shrink[Simple]]
    //   compare(shrink, expectedSimpleShrink)
    // }

  }

}
