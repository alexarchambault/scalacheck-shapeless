
import Aliases._
import Settings._

import sbtcrossproject.crossProject

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
