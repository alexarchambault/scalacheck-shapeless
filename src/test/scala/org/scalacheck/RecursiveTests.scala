package org.scalacheck

import Shapeless._

object RecursiveTests extends Properties("RecursiveTests") {
  private val ok = (_: Any) => true

  object T1 {
    sealed abstract class Tree
    final case class Node(left: Tree, right: Tree, v: Int) extends Tree
    case object Leaf extends Tree
  }

  object T2 {
    sealed abstract class Tree
    case class Node(left: Tree, right: Tree, v: Int) extends Tree
    case object Leaf extends Tree
  }

  property("open hierarchy") = {
    Prop.forAll(implicitly[Arbitrary[T2.Tree]].arbitrary)(ok)
  }

  property("closed hierarchy") = {
    Prop.forAll(implicitly[Arbitrary[T1.Tree]].arbitrary)(ok)
  }
}
