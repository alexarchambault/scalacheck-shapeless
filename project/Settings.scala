import sbt._
import sbt.Keys._

import Aliases._

object Settings {

  private def scala210 = "2.10.6"
  private def scala211 = "2.11.12"
  private def scala212 = "2.12.4"
  private def scala213 = "2.13.0-M5"

  lazy val shared = Seq(
    scalaVersion := scala212,
    crossScalaVersions := Seq(scala213, scala212, scala211, scala210),
    resolvers += Resolver.sonatypeRepo("releases"),
    libs ++= {
      if (scalaBinaryVersion.value == "2.10")
        Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch))
      else
        Seq()
    },
    scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "2.10" | "2.11" =>
          Seq("-target:jvm-1.7")
        case _ =>
          Nil
      }
    }
  )

  lazy val dontPublish = Seq(
    publish := {},
    publishLocal := {},
    publishArtifact := false
  )

  lazy val utest = Seq(
    libs += Deps.utest.value % "test",
    testFrameworks += new TestFramework("utest.runner.Framework")
  )

}
