package org.scalacheck

import shapeless.{ Lazy => _, _ }
import shapeless.compat._

import utest._

import Util.validateSingletons

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
