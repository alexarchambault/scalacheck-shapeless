package org.scalacheck

class ShrinkTests extends Properties("ShrinkTests") {

  property("Lists should shrink as a container, not a coproduct") = {
    val defaultShrink = implicitly[Shrink[List[String]]]
    val shrink = { import Shapeless._; implicitly[Shrink[List[String]]] }

    Prop.forAll(Arbitrary.arbitrary[List[String]]) { l =>
      defaultShrink.shrink(l) == shrink.shrink(l)
    }
  }

  property("Options should shrink specifically, not as a coproduct") = {
    val defaultShrink = implicitly[Shrink[Option[String]]]
    val shrink = { import Shapeless._; implicitly[Shrink[Option[String]]] }

    Prop.forAll(Arbitrary.arbitrary[Option[String]]) { o =>
      defaultShrink.shrink(o) == shrink.shrink(o)
    }
  }

}
