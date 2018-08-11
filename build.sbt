
import Aliases._
import Settings._

import sbtcrossproject.crossProject

inThisBuild(List(
  organization := "com.github.alexarchambault",
  homepage := Some(url("https://github.com/alexarchambault/scalacheck-shapeless")),
  licenses := Seq("Apache 2.0" -> url("http://opensource.org/licenses/Apache-2.0")),
  developers := List(Developer(
    "alexarchambault",
    "Alexandre Archambault",
    "",
    url("https://github.com/alexarchambault")
  ))
))

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .settings(
    shared,
    name := "scalacheck-shapeless_1.14",
    moduleName := name.value, // keep the '.' in name ^
    libs ++= Seq(
      Deps.scalacheck.value,
      Deps.shapeless.value
    ),
    mimaPreviousArtifacts := {
      Seq[String]()
        .map(v => organization.value %% moduleName.value % v)
        .toSet
    }
  )
  .jsSettings(
    scalaJSStage.in(Test) := FastOptStage
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val test = crossProject(JSPlatform, JVMPlatform)
  .dependsOn(core)
  .settings(
    shared,
    dontPublish,
    utest
  )
  .jsSettings(
    scalaJSStage.in(Test) := FastOptStage
  )

lazy val testJVM = test.jvm
lazy val testJS = test.js


lazy val `scalacheck-shapeless` = project
  .in(root)
  .aggregate(
    coreJVM,
    coreJS,
    testJVM,
    testJS
  )
  .settings(
    shared,
    dontPublish
  )
