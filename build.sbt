import com.typesafe.sbt.pgp.PgpKeys
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact

lazy val root = project.in(file("."))
  .aggregate(coreJVM, coreJS)
  .settings(commonSettings)
  .settings(noPublishSettings)

lazy val core = crossProject
  .settings(commonSettings: _*)
  .settings(mimaSettings: _*)
  .jsSettings(
    postLinkJSEnv := NodeJSEnv().value,
    scalaJSUseRhino in Global := false,
    scalaJSStage in Test := FastOptStage
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val coreName = "scalacheck-shapeless_1.13"

lazy val commonSettings = Seq(
  organization := "com.github.alexarchambault",
  name := coreName,
  moduleName := coreName
) ++ compileSettings ++ publishSettings

lazy val compileSettings = Seq(
  scalaVersion := "2.11.8",
  unmanagedSourceDirectories in Compile += (baseDirectory in Compile).value / ".." / "shared" / "src" / "main" / s"scala-${scalaBinaryVersion.value}",
  libraryDependencies += "com.lihaoyi" %%% "utest" % "0.3.0" % "test",
  testFrameworks += new TestFramework("utest.runner.Framework"),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck" % "1.13.1",
    "com.chuusai" %%% "shapeless" % "2.2.5",
    "com.github.alexarchambault" %%% "shapeless-compat" % "1.0.0-M4"
  ),
  libraryDependencies ++= {
    if (scalaVersion.value.startsWith("2.10."))
      Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full))
    else
      Seq()
  },
  scalacOptions += "-target:jvm-1.7"
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
