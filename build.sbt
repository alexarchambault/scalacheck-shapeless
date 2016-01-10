import com.typesafe.sbt.pgp.PgpKeys
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact

lazy val root = project.in(file("."))
  .aggregate(scalacheckShapelessJVM, scalacheckShapelessJS)
  .settings(commonSettings)
  .settings(compileSettings)
  .settings(noPublishSettings)

lazy val scalacheckShapeless = crossProject.in(file("."))
  .settings(commonSettings: _*)
  .settings(compileSettings: _*)
  .settings(publishSettings: _*)
  .settings(mimaSettings: _*)
  .jsSettings(scalaJSStage in Test := FastOptStage)

lazy val scalacheckShapelessJVM = scalacheckShapeless.jvm
lazy val scalacheckShapelessJS = scalacheckShapeless.js

lazy val coreName = "scalacheck-shapeless_1.13"

lazy val commonSettings = Seq(
  organization := "com.github.alexarchambault",
  name := coreName,
  moduleName := coreName
)

lazy val compileSettings = Seq(
  scalaVersion := "2.11.7",
  crossScalaVersions := Seq("2.10.6", "2.11.7"),
  unmanagedSourceDirectories in Compile += (baseDirectory in Compile).value / ".." / "shared" / "src" / "main" / s"scala-${scalaBinaryVersion.value}",
  libraryDependencies += "com.lihaoyi" %%% "utest" % "0.3.0" % "test",
  testFrameworks += new TestFramework("utest.runner.Framework"),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck" % "1.13.0-bceacad-SNAPSHOT",
    "com.chuusai" %%% "shapeless" % "2.3.0-SNAPSHOT",
    "com.github.alexarchambault" %%% "derive" % "0.1.0-SNAPSHOT",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
  ),
  libraryDependencies ++= {
    if (scalaVersion.value.startsWith("2.10."))
      Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full))
    else
      Seq()
  }
)

lazy val publishSettings = Seq(
  homepage := Some(url("https://github.com/alexarchambault/scalacheck-shapeless")),
  licenses := Seq(
    "Apache 2.0" -> url("http://opensource.org/licenses/Apache-2.0")
  ),
  scmInfo := Some(ScmInfo(
    url("https://github.com/alexarchambault/scalacheck-shapeless.git"),
    "scm:git:github.com/alexarchambault/scalacheck-shapeless.git",
    Some("scm:git:git@github.com:alexarchambault/scalacheck-shapeless.git")
  )),
  developers := List(Developer(
    "alexarchambault",
    "Alexandre Archambault",
    "",
    url("https://github.com/alexarchambault")
  )),
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  publishTo := Some {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      "snapshots" at nexus + "content/repositories/snapshots"
    else
      "releases" at nexus + "service/local/staging/deploy/maven2"
  },
  credentials += {
    Seq("SONATYPE_USER", "SONATYPE_PASS").map(sys.env.get) match {
      case Seq(Some(user), Some(pass)) =>
        Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)
      case _ =>
        Credentials(Path.userHome / ".ivy2" / ".credentials")
    }
  }
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val mimaSettings =
  mimaDefaultSettings ++
  Seq(
    previousArtifact := Some(organization.value %% moduleName.value % "1.0.0")
  )

// build.sbt shamelessly inspired by https://github.com/fthomas/refined/blob/master/build.sbt
