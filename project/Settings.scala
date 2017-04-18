import sbt._
import sbt.Keys._

import Aliases._

object Settings {

  lazy val shared = Seq(
    resolvers += Resolver.sonatypeRepo("releases"),
    libs ++= {
      if (scalaBinaryVersion.value == "2.10")
        Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch))
      else
        Seq()
    },
    scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "2.10" | "2.11" =>
          Seq("-target:jvm-1.7")
        case _ =>
          Nil
      }
    },
    organization := "com.github.alexarchambault",
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

  lazy val dontPublish = Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false
  )

  lazy val utest = Seq(
    libs += Deps.utest.value % "test",
    testFrameworks += new TestFramework("utest.runner.Framework")
  )

}
