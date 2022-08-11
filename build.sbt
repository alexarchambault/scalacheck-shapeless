
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

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .settings(
    scalaVersion := Scala.scala213,
    crossScalaVersions := Scala.all,
    scalacOptions ++= Seq(
      "-deprecation"
    ),
    name := "scalacheck-shapeless_1.16",
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
    (Test / scalaJSStage) := FastOptStage
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js


disablePlugins(MimaPlugin)
(publish / skip) := true
crossScalaVersions := Nil
