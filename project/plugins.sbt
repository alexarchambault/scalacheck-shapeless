plugins_(
  "com.geirsson"       % "sbt-ci-release"           % "1.5.7",
  "org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0"
)

def plugins_(modules: ModuleID*) = modules.map(addSbtPlugin)

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.8.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.3.1")
