package org.scalacheck

object SizeTestsDefinitions {
  // see https://github.com/rickynils/scalacheck/issues/305
  sealed trait Tree {
    def depth: Int = {

      var max = 0
      val m = new scala.collection.mutable.Queue[(Int, Branch)]

      def handle(t: Tree, s: Int) =
        t match {
          case Leaf => max = max max s
          case b: Branch =>
            m += (s + 1) -> b
        }

      handle(this, 0)

      while (m.nonEmpty) {
        val (s, b) = m.dequeue()
        handle(b.left, s)
        handle(b.right, s)
      }

      max
    }
  }
  case object Leaf extends Tree
  case class Branch(left: Tree, right: Tree) extends Tree

  object Tree {
    implicit val recursive = derive.Recursive[Tree](Gen.const(Leaf))
  }
}