
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
    libraryDependencies ++= Seq(
      Deps.scalacheck.value,
      Deps.shapeless.value
    ),
    mimaPreviousArtifacts := Set.empty
  )
  .jsSettings(
    scalaJSStage.in(Test) := FastOptStage
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val test = crossProject(JSPlatform, JVMPlatform)
  .dependsOn(core)
  .disablePlugins(MimaPlugin)
  .settings(
    shared,
    skip.in(publish) := true,
    utest
  )
  .jsSettings(
    scalaJSStage.in(Test) := FastOptStage
  )

lazy val testJVM = test.jvm
lazy val testJS = test.js


disablePlugins(MimaPlugin)
skip.in(publish) := true
crossScalaVersions := Nil
