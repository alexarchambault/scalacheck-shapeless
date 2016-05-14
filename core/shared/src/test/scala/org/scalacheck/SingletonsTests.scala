package org.scalacheck

import shapeless.{ Lazy => _, _ }
import shapeless.compat._

import utest._

import Util.validateSingletons

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

object SingletonsTests extends TestSuite {
  import SingletonsTestsDefinitions._

  val tests = TestSuite {
    'hnil - {
      validateSingletons[HNil](HNil)
    }

    'caseObject - {
      validateSingletons[CaseObj.type](CaseObj)
    }

    'emptyCaseClass - {
      validateSingletons[Empty](Empty())
    }

    'adt - {
      validateSingletons[Base](BaseEmpty(), BaseObj)
    }

    'adtNotAllSingletons - {
      validateSingletons[BaseMore](BaseMoreEmpty(), BaseMoreObj)
    }

    'nonSingletonCaseClass - {
      validateSingletons[NonSingleton]()
    }
  }

}
