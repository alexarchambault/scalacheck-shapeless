package org.scalacheck

import derive._

trait ScalacheckShapeless
  extends SingletonInstances
  with HListInstances
  with CoproductInstances
  with DerivedInstances
  with FieldTypeInstances
  with EnumerationInstances

object ScalacheckShapeless extends ScalacheckShapeless

@deprecated("Use ScalacheckShapeless instead", "1.1.6")
object Shapeless extends ScalacheckShapeless
