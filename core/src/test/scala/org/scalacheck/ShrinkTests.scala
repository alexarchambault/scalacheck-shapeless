package org.scalacheck

import utest._
import Util._

object ShrinkTests extends TestSuite {

  val tests = TestSuite {
    'listContainer {
      val defaultShrink = implicitly[Shrink[List[String]]]
      val shrink = { import Shapeless._; implicitly[Shrink[List[String]]] }

      Prop.forAll(Arbitrary.arbitrary[List[String]]) { l =>
        defaultShrink.shrink(l) == shrink.shrink(l)
      }
      .validate
    }

    'optionDefaultShrink {
      val defaultShrink = implicitly[Shrink[Option[String]]]
      val shrink = { import Shapeless._; implicitly[Shrink[Option[String]]] }

      Prop.forAll(Arbitrary.arbitrary[Option[String]]) { o =>
        defaultShrink.shrink(o) == shrink.shrink(o)
      }
      .validate
    }
  }

}
