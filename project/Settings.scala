import sbt._
import sbt.Keys._

import Aliases._

object Settings {

  private def scala211 = "2.11.12"
  private def scala212 = "2.12.12"
  private def scala213 = "2.13.6"

  lazy val shared = Def.settings(
    scalaVersion := scala212,
    crossScalaVersions := Seq(scala213, scala212, scala211),
    crossScalaVersions := {
      val former = crossScalaVersions.value
      if (isScalaJs1.value)
        former.filter(!_.startsWith("2.11."))
      else
        former
    },
    scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "2.11" =>
          Seq("-target:jvm-1.7")
        case _ =>
          Nil
      }
    }
  )

  lazy val utest = Def.settings(
    libs += Deps.utest.value % "test",
    testFrameworks += new TestFramework("utest.runner.Framework")
  )

  lazy val isScalaJs1 = Def.setting {
    def scalaJsVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.0.0")
    sbtcrossproject.CrossPlugin.autoImport.crossProjectPlatform.?.value.contains(scalajscrossproject.JSPlatform) &&
      scalaJsVersion.startsWith("1.")
  }

}
