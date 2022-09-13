import sbt._
import sbt.Keys.scalaVersion

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Deps {

  import Def.setting

  def scalacheck = setting("org.scalacheck" %%% "scalacheck" % "1.16.0")
  def shapeless = setting("com.chuusai" %%% "shapeless" % "2.3.9")
  def utest = setting("com.lihaoyi" %%% "utest" % "0.8.1")
}
