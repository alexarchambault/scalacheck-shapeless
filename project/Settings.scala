import sbt._
import sbt.Keys._

object Settings {

  private def scala212 = "2.12.13"
  private def scala213 = "2.13.3"

  lazy val shared = Def.settings(
    scalaVersion := scala212,
    crossScalaVersions := Seq(scala213, scala212)
  )
}
