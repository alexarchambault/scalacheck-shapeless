# scalacheck-shapeless

Generation of arbitrary case classes / ADTs instances with [scalacheck](https://github.com/rickynils/scalacheck) and [shapeless](https://github.com/milessabin/shapeless)

[![Build Status](https://travis-ci.org/alexarchambault/scalacheck-shapeless.svg)](https://travis-ci.org/alexarchambault/scalacheck-shapeless)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/alexarchambault/scalacheck-shapeless?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.alexarchambault/scalacheck-shapeless_1.13_2.11.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.alexarchambault/scalacheck-shapeless_1.13_2.11)

## Usage

Add to your `build.sbt`
```scala
resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies += "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.1"
```

If you are using scala 2.10.x, also add the macro paradise plugin to your build,
```scala
libraryDependencies +=
  compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
```

scalacheck-shapeless depends on shapeless 2.3 and scalacheck 1.13. If you are using shapeless 2.2
along with scalacheck 1.13, use the `1.0.0` version. If you are using shapeless 2.2 along
with scalacheck 1.12, see the [0.3.x branch](https://github.com/alexarchambault/scalacheck-shapeless/tree/scalacheck-shapeless-0.3).


Import the content of `org.scalacheck.Shapeless` close to where you want
`Arbitrary` type classes to be automatically available for case classes
/ sealed hierarchies,
```scala
import org.scalacheck.Shapeless._

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

For the development version, add instead to your `build.sbt`
```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies +=
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.2-SNAPSHOT"
```

(Macro paradise plugin also necessary with scala 2.10, see above.)

## See also

- [scalacheck-datetime](https://github.com/47deg/scalacheck-datetime), a library to deal with datetimes with scalacheck,
- [scalacheck-extensions](https://github.com/cvogt/scalacheck-extensions), a macro-based automatic `Arbitrary` generation (discontinued?).
