package org.scalacheck

import shapeless.{test => _, _}

import utest._

import Util.validateSingletons

object SingletonsTests extends TestSuite {
  import SingletonsTestsDefinitions._

  val tests = TestSuite {
    test("hnil") {
      validateSingletons[HNil](HNil)
    }

    test("caseObject") {
      validateSingletons[CaseObj.type](CaseObj)
    }

    test("emptyCaseClass") {
      validateSingletons[Empty](Empty())
    }

    test("adt") {
      validateSingletons[Base](BaseEmpty(), BaseObj)
    }

    test("adtNotAllSingletons") {
      validateSingletons[BaseMore](BaseMoreEmpty(), BaseMoreObj)
    }

    test("nonSingletonCaseClass") {
      validateSingletons[NonSingleton]()
    }
  }

}
