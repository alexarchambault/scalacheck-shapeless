# scalacheck-shapeless

Generation of arbitrary case classes / ADTs instances with [scalacheck](https://github.com/typelevel/scalacheck) and [shapeless](https://github.com/milessabin/shapeless)

[![Build status](https://github.com/alexarchambault/scalacheck-shapeless/workflows/CI/badge.svg)](https://github.com/alexarchambault/scalacheck-shapeless/actions?query=workflow%3ACI)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.alexarchambault/scalacheck-shapeless_1.16_2.13.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.alexarchambault/scalacheck-shapeless_1.16_2.13)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/alexarchambault/scalacheck-shapeless?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Usage

Add to your `build.sbt`
```scala
libraryDependencies += "com.github.alexarchambault" %% "scalacheck-shapeless_1.16" % "1.3.1"
```

scalacheck-shapeless depends on shapeless 2.3 and scalacheck 1.16. It is built against scala 2.12, and 2.13.

Import the content of `org.scalacheck.ScalacheckShapeless` close to where you want
`Arbitrary` type classes to be automatically available for case classes
/ sealed hierarchies,
```scala
import org.scalacheck.ScalacheckShapeless._

//  If you defined:

// case class Foo(i: Int, s: String, blah: Boolean)
// case class Bar(foo: Foo, other: String)

// sealed trait Base
// case class BaseIntString(i: Int, s: String) extends Base
// case class BaseDoubleBoolean(d: Double, b: Boolean) extends Base

//  then you can now do

implicitly[Arbitrary[Foo]]
implicitly[Arbitrary[Bar]]
implicitly[Arbitrary[Base]]
```

and in particular, while writing property-based tests,
```scala
property("some property about Foo") {
  forAll { foo: Foo =>
    // Ensure foo has the required property
  }
}
```
without having to define yourself an `Arbitrary` for `Foo`.

## See also

- [cats-check](https://github.com/non/cats-check), a library providing cats type class instances for ScalaCheck type classes,
- [scalacheck-datetime](https://github.com/47deg/scalacheck-datetime), a library to deal with datetimes with scalacheck,
- [scalacheck-extensions](https://github.com/cvogt/scalacheck-extensions), a macro-based automatic `Arbitrary` generation (discontinued?).

## License

Released under the Apache 2 license. See LICENSE file for more details.

## Code of Conduct

See the [Code of Conduct](CODE_OF_CONDUCT.md)
