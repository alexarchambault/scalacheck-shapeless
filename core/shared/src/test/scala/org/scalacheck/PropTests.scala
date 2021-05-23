package org.scalacheck

import utest._
import Util._
import ScalacheckShapeless._
import org.scalacheck.derive.{MkArbitrary, Recursive}
import org.scalacheck.TestsDefinitions.{T1, T1NoRecursiveTC}

object PropTests extends TestSuite {

  val tests = TestSuite {
    test("oneElementAdt") {
      sealed trait Foo
      case object Bar extends Foo

      val prop = Prop.forAll { (f: Int => Foo) => f(0); true }

      prop.validate()
    }

    test("twoElementAdt") {
      sealed trait Or[+A, +B] extends Product with Serializable
      case class Left[A](a: A) extends Or[A, Nothing]
      case class Right[B](b: B) extends Or[Nothing, B]

      val prop = Prop.forAll { (f: Int => Float Or Boolean) => f(0); true }

      prop.validate()
    }

    test("recursiveADT") {
      case class Node[T](value: T, left: Option[Node[T]], right: Option[Node[T]])

      // deriving the Arbitrary[Option[…]] ourselves, so that it safely
      // unties the recursion
      implicit def rec[T]: Recursive[Option[Node[T]]] =
        Recursive[Option[Node[T]]](Gen.const(None))
      implicit def arb[T: Arbitrary]: Arbitrary[Option[Node[T]]] =
        MkArbitrary[Option[Node[T]]].arbitrary

      val prop = Prop.forAll { (f: Int => Node[Int]) => f(0); true }

      prop.validate()
    }

    test("recursiveADT1") {
      val prop = Prop.forAll { (f: Int => T1.Tree) => f(0); true }

      prop.validate(10000)
    }

    test("recursiveADT2") {
      val prop = Prop.forAll { (f: Int => T1NoRecursiveTC.Tree) => f(0); true }

      // FIXME Doesn't fail with scalacheck 1.14.0, because of its retry mechanism
      // scalacheck retries to generate stuff when it gets stackoverflows
      // prop.mustFail(10000)
    }
  }

}
