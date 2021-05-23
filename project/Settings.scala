import sbt._
import sbt.Keys._

object Settings {

  private def scala212 = "2.12.13"
  private def scala213 = "2.13.3"

  lazy val shared = Def.settings(
    scalaVersion := scala212,
    crossScalaVersions := Seq(scala213, scala212)
  )

  lazy val utest = Def.settings(
    libraryDependencies += Deps.utest.value % "test",
    testFrameworks += new TestFramework("utest.runner.Framework")
  )

  lazy val isScalaJs1 = Def.setting {
    sbtcrossproject.CrossPlugin.autoImport.crossProjectPlatform.?.value.contains(scalajscrossproject.JSPlatform)
  }

}
