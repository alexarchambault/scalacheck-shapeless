package org.scalacheck

object SingletonsTestsDefinitions {

  // Running into SI-7046 for Base if these are put directly into SingletonsTests

  case object CaseObj
  case class Empty()

  sealed trait Base
  case object BaseObj extends Base
  case class BaseEmpty() extends Base

  sealed trait BaseMore
  case object BaseMoreObj extends BaseMore
  case class BaseMoreEmpty() extends BaseMore
  case class BaseMoreNonSingleton(i: Int) extends BaseMore

  case class NonSingleton(s: String)

}
