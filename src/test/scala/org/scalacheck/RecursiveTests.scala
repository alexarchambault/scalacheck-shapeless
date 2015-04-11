package org.scalacheck

import Definitions._
import Instances._

object RecursiveTests extends Properties("RecursiveTests") {
  private val ok = (_: Any) => true

  property("open hierarchy") = {
    Prop.forAll(implicitly[Arbitrary[T2.Tree]].arbitrary)(ok)
  }

  property("closed hierarchy") = {
    Prop.forAll(implicitly[Arbitrary[T1.Tree]].arbitrary)(ok)
  }
}
