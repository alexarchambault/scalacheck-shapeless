import sbt._
import sbt.Keys.scalaVersion

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Deps {

  import Def.setting

  def scalacheck = setting("org.scalacheck" %%% "scalacheck" % "1.14.0")
  def shapeless = setting("com.chuusai" %%% "shapeless" % "2.3.3")
  def utest = setting {
    val sv = scalaVersion.value
    val ver =
      if (sv.startsWith("2.10.") || sv.startsWith("2.11.")) "0.6.7"
      else "0.6.9"
    "com.lihaoyi" %%% "utest" % ver
  }
}
