plugins_(
  "com.geirsson"       % "sbt-ci-release"           % "1.5.0",
  "com.typesafe"       % "sbt-mima-plugin"          % "0.3.0",
  "org.scala-js"       % "sbt-scalajs"              % "0.6.32",
  "org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.1"
)

def plugins_(modules: ModuleID*) = modules.map(addSbtPlugin)
