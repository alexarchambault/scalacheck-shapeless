plugins_(
  "com.geirsson"       % "sbt-ci-release"           % "1.5.5",
  "org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0"
)

def plugins_(modules: ModuleID*) = modules.map(addSbtPlugin)

val scalaJsVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.3.1")

addSbtPlugin("io.github.alexarchambault.sbt" % "sbt-eviction-rules" % "0.2.0")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.8.1")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJsVersion)
