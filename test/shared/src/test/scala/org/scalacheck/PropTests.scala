package org.scalacheck

import utest._
import Util._
import ScalacheckShapeless._
import org.scalacheck.TestsDefinitions.{T1, T1NoRecursiveTC}

object PropTests extends TestSuite {

  val tests = TestSuite {
    'oneElementAdt - {
      sealed trait Foo
      case object Bar extends Foo

      val prop = Prop.forAll { (f: Int => Foo) => f(0); true }

      prop.validate()
    }

    'twoElementAdt - {
      sealed trait Or[+A, +B] extends Product with Serializable
      case class Left[A](a: A) extends Or[A, Nothing]
      case class Right[B](b: B) extends Or[Nothing, B]

      val prop = Prop.forAll { (f: Int => Float Or Boolean) => f(0); true }

      prop.validate()
    }

    'recursiveADT - {
      case class Node[T](value: T, left: Option[Node[T]], right: Option[Node[T]])

      val prop = Prop.forAll { (f: Int => Node[Int]) => f(0); true }

      prop.validate()
    }

    'recursiveADT1 - {
      val prop = Prop.forAll { (f: Int => T1.Tree) => f(0); true }

      prop.validate(10000)
    }

    'recursiveADT2 - {
      val prop = Prop.forAll { (f: Int => T1NoRecursiveTC.Tree) => f(0); true }

      prop.mustFail(10000)
    }
  }

}
