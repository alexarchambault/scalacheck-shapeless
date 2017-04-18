import sbt._

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Deps {

  import Def.setting

  def scalacheck = setting("org.scalacheck" %%% "scalacheck" % "1.13.5")
  def shapeless = setting("com.chuusai" %%% "shapeless" % "2.3.2")
  def utest = setting("com.lihaoyi" %%% "utest" % "0.4.4")
}