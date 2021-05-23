
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
    scalaVersion := Scala.scala212,
    crossScalaVersions := Scala.all,
    name := "scalacheck-shapeless_1.14",
    moduleName := name.value, // keep the '.' in name ^
    libraryDependencies ++= Seq(
      Deps.scalacheck.value,
      Deps.shapeless.value,
      Deps.utest.value % Test
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    mimaPreviousArtifacts := Set.empty
  )
  .jsSettings(
    scalaJSStage.in(Test) := FastOptStage
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js


disablePlugins(MimaPlugin)
skip.in(publish) := true
crossScalaVersions := Nil
