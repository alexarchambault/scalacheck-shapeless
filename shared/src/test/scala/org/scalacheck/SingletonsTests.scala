package org.scalacheck

import shapeless._
import utest._

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

  def validate[T: Singletons](expected: T*) = {
    val found = Singletons[T].apply()
    assert(found == expected)
  }

  val tests = TestSuite {
    'hnil - {
      validate[HNil](HNil)
    }

    'caseObject - {
      validate[CaseObj.type](CaseObj)
    }

    'emptyCaseClass - {
      validate[Empty](Empty())
    }

    'adt - {
      validate[Base](BaseEmpty(), BaseObj)
    }

    'adtNotAllSingletons - {
      validate[BaseMore](BaseMoreEmpty(), BaseMoreObj)
    }

    'nonSingletonCaseClass - {
      validate[NonSingleton]()
    }
  }

}
