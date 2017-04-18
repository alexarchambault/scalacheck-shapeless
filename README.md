# scalacheck-shapeless

Generation of arbitrary case classes / ADTs instances with [scalacheck](https://github.com/rickynils/scalacheck) and [shapeless](https://github.com/milessabin/shapeless)

[![Build Status](https://travis-ci.org/alexarchambault/scalacheck-shapeless.svg)](https://travis-ci.org/alexarchambault/scalacheck-shapeless)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/alexarchambault/scalacheck-shapeless?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.alexarchambault/scalacheck-shapeless_1.13_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.alexarchambault/scalacheck-shapeless_1.13_2.12)

## Usage

Add to your `build.sbt`
```scala
resolvers += Resolver.sonatypeRepo("releases") // only needed if the release hasn't reached maven central yet

libraryDependencies += "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.5"
```

If you are using scala 2.10.x, also add the macro paradise plugin to your build,
```scala
libraryDependencies +=
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)
```

scalacheck-shapeless depends on shapeless 2.3 and scalacheck 1.13. It is built against scala 2.10, 2.11, and 2.12.

If you are using shapeless 2.2 along with scalacheck 1.13, use the `1.0.0` version. If you are
using shapeless 2.2 along with scalacheck 1.12, see the
[0.3.x branch](https://github.com/alexarchambault/scalacheck-shapeless/tree/scalacheck-shapeless-0.3).



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
