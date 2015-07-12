import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

import sbt._, Keys._

import sbtrelease.ReleasePlugin._
import com.typesafe.sbt.pgp.PgpKeys
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact


object ScalacheckShapelessBuild extends Build {

  private lazy val commonSettings: Seq[Setting[_]] =
    Seq(
      organization := "com.github.alexarchambault",
      scalaVersion := "2.11.7",
      crossScalaVersions := Seq("2.10.5", "2.11.7"),
      resolvers ++= Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots")
      )
    ) ++
    publishingSettings ++
    Seq(
      libraryDependencies += "com.lihaoyi" %%% "utest" % "0.3.0" % "test",
      testFrameworks += new TestFramework("utest.runner.Framework")
    )

  private lazy val publishingSettings = Seq[Setting[_]](
    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra := {
      <url>https://github.com/alexarchambault/scalacheck-shapeless</url>
        <licenses>
          <license>
            <name>Apache 2.0</name>
            <url>http://opensource.org/licenses/Apache-2.0</url>
          </license>
        </licenses>
        <scm>
          <connection>scm:git:github.com/alexarchambault/scalacheck-shapeless.git</connection>
          <developerConnection>scm:git:git@github.com:alexarchambault/scalacheck-shapeless.git</developerConnection>
          <url>github.com/alexarchambault/scalacheck-shapeless.git</url>
        </scm>
        <developers>
          <developer>
            <id>alexarchambault</id>
            <name>Alexandre Archambault</name>
            <url>https://github.com/alexarchambault</url>
          </developer>
        </developers>
    },
    credentials += {
      Seq("SONATYPE_USER", "SONATYPE_PASS").map(sys.env.get) match {
        case Seq(Some(user), Some(pass)) =>
          Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)
        case _ =>
          Credentials(Path.userHome / ".ivy2" / ".credentials")
      }
    }
  ) ++
  releaseSettings ++
  Seq(
    ReleaseKeys.versionBump := sbtrelease.Version.Bump.Bugfix,
    sbtrelease.ReleasePlugin.ReleaseKeys.publishArtifactsAction := PgpKeys.publishSigned.value
  )

  private lazy val commonJsSettings = Seq[Setting[_]](
    postLinkJSEnv := NodeJSEnv().value,
    scalaJSStage in Global := FastOptStage
  )

  private lazy val mimaSettings =
    mimaDefaultSettings ++
    Seq(
      previousArtifact := Some(organization.value %% moduleName.value % "1.0.0")
    )


  private val coreName = "scalacheck-shapeless_1.13"
  private lazy val coreSettings =
    commonSettings ++
    // Uncomment once 1.0.0 is released
    // mimaSettings ++
    Seq(
      moduleName := coreName,
      name := coreName,
      unmanagedSourceDirectories in Compile += (baseDirectory in LocalRootProject).value / "core" / "src" / "main" / "scala",
      unmanagedSourceDirectories in Test += (baseDirectory in LocalRootProject).value / "core" / "src" / "test" / "scala",
      libraryDependencies ++= Seq(
        "org.scalacheck" %%% "scalacheck" % "1.13.0-e5cb830-SNAPSHOT",
        "com.github.alexarchambault" %%% "shapeless" % "2.2.5-aa-SNAPSHOT"
      ),
      libraryDependencies ++= {
        if (scalaVersion.value.startsWith("2.10."))
          Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full))
        else
          Seq()
      }
    )


  lazy val coreJvm = Project(id = "core-jvm", base = file("core-jvm"))
    .settings(coreSettings: _*)

  lazy val coreJs = Project(id = "core-js", base = file("core-js"))
    .enablePlugins(ScalaJSPlugin)
    .settings(coreSettings: _*)
    .settings(commonJsSettings: _*)

  lazy val root = Project(id = "root", base = file("."))
    .aggregate(coreJvm, coreJs)
    .settings(commonSettings: _*)
    .settings(
      (unmanagedSourceDirectories in Compile) := Nil,
      (unmanagedSourceDirectories in Test) := Nil,
      publish := (),
      publishLocal := ()
    )

}
