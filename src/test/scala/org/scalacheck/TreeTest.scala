package org.scalacheck
import Shapeless._

object Examples211 extends Properties("Examples211") {
  object T1{  
    sealed abstract class Tree
    final case class Node(left: Tree, right: Tree, v: Int) extends Tree
    case object Leaf extends Tree
  }

  sealed trait A
  sealed case class B(i: Int, s: String) extends A
  case object C extends A
  sealed trait D extends A
  final case class E(a: Double, b: Option[Float]) extends D
  case object F extends D
  sealed abstract class Foo extends D
  case object Baz extends Foo
  /*final class Bar extends Foo
  final class Baz(i1: Int)(s1: String) extends Foo*/
  
  property("closed hierarchy") = {
    import T1._
    Prop.forAll(implicitly[Arbitrary[Tree]].arbitrary){
      case Leaf => true
      case Node(_,_,_) => true
    }


    Prop.forAll(implicitly[Arbitrary[A]].arbitrary){
      case _:A => true
    }

    /*Prop.forAll(implicitly[Arbitrary[D]].arbitrary){
      case _:D => true
    }*/
  }

  object T2{  
    sealed abstract class Tree
    case class Node(left: Tree, right: Tree, v: Int) extends Tree
    case object Leaf extends Tree
  }
  property("open hierarchy") = {
    import T2._
    Prop.forAll(implicitly[Arbitrary[Tree]].arbitrary){
      case Leaf => true
      case Node(_,_,_) => true
    }
  }
}
