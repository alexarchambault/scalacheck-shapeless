package org.scalacheck

import Definitions._
import Instances._

import utest._
import Util._

object RecursiveTests extends TestSuite {
  private val ok = (_: Any) => true

  val tests = TestSuite {
    'openHierarchy {
      Prop.forAll(implicitly[Arbitrary[T2.Tree]].arbitrary)(ok)
        .validate
    }

    'closedHierarchy {
      Prop.forAll(implicitly[Arbitrary[T1.Tree]].arbitrary)(ok)
        .validate
    }
  }
}
