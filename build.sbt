
lazy val `scalacheck-shapeless` = project
  .in(file("."))
  .aggregate(coreJVM, coreJS, testJVM, testJS)
  .settings(commonSettings)
  .settings(noPublishSettings)

lazy val core = crossProject
  .settings(commonSettings)
  .settings(
    name := coreName,
    moduleName := coreName,
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.13.4",
      "com.chuusai" %%% "shapeless" % "2.3.2"
    ),
    mimaPreviousArtifacts := {
      if (scalaBinaryVersion.value == "2.12")
        Set()
      else
        Set(organization.value %% moduleName.value % "1.1.0")
    }
  )
  .jsSettings(
    postLinkJSEnv := NodeJSEnv().value,
    scalaJSUseRhino in Global := false,
    scalaJSStage in Test := FastOptStage
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val test = crossProject
  .dependsOn(core)
  .settings(commonSettings)
  .settings(noPublishSettings)
  .settings(
    libraryDependencies += "com.lihaoyi" %%% "utest" % "0.4.4" % "test",
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .jsSettings(
    scalaJSStage in Test := FastOptStage
  )

lazy val testJVM = test.jvm
lazy val testJS = test.js

lazy val coreName = "scalacheck-shapeless_1.13"

lazy val commonSettings = Seq(
  organization := "com.github.alexarchambault"
) ++ compileSettings ++ publishSettings

lazy val compileSettings = Seq(
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  libraryDependencies ++= {
    if (scalaVersion.value.startsWith("2.10."))
      Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))
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
  credentials ++= {
    Seq("SONATYPE_USER", "SONATYPE_PASS").map(sys.env.get) match {
      case Seq(Some(user), Some(pass)) =>
        Seq(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass))
      case _ =>
        Seq()
    }
  }
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

// build.sbt shamelessly inspired by https://github.com/fthomas/refined/blob/master/build.sbt
