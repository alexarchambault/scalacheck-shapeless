organization := "com.github.alexarchambault"

name := "scalacheck-shapeless"

version := "1.12.1-SNAPSHOT" // Following scalacheck versions here

scalaVersion := "2.11.4"

crossScalaVersions := Seq("2.10.4", "2.11.4")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.12.1"
)

libraryDependencies ++= {
  if (scalaVersion.value startsWith "2.10.")
    Seq(
      "com.chuusai" %% "shapeless" % "2.1.0-SNAPSHOT" cross CrossVersion.full,
      // For shapeless LabelledGeneric to work, you may need to add it
      // to your own project too...
      compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
    )
  else
    Seq(
      "com.chuusai" %% "shapeless" % "2.1.0-SNAPSHOT"
    )
}


xerial.sbt.Sonatype.sonatypeSettings

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

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
}

