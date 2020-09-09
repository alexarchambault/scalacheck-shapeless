plugins_(
  "com.geirsson"       % "sbt-ci-release"           % "1.5.3",
  "org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0"
)

def plugins_(modules: ModuleID*) = modules.map(addSbtPlugin)

val scalaJsVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.2.0")

addSbtPlugin(("io.github.alexarchambault.sbt" % "sbt-compatibility" % "0.0.8").exclude("com.typesafe", "sbt-mima-plugin"))
addSbtPlugin("io.github.alexarchambault.sbt" % "sbt-eviction-rules" % "0.2.0")
addSbtPlugin("com.github.alexarchambault.tmp" % "sbt-mima-plugin" % "0.7.1-SNAPSHOT")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJsVersion)

resolvers += Resolver.sonatypeRepo("snapshots")
