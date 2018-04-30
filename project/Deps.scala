import sbt._

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Deps {

  import Def.setting

  def scalacheck = setting("org.scalacheck" %%% "scalacheck" % "1.14.0")
  def shapeless = setting("com.chuusai" %%% "shapeless" % "2.3.3")
  def utest = setting("com.lihaoyi" %%% "utest" % "0.6.4")
}
