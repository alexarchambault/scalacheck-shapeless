import sbt._
import sbt.Keys.scalaVersion

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Deps {

  import Def.setting

  def scalacheck = setting("org.scalacheck" %%% "scalacheck" % "1.15.4")
  def shapeless = setting("com.chuusai" %%% "shapeless" % "2.3.7")
  def utest = setting("com.lihaoyi" %%% "utest" % "0.7.4")
}
