plugins_(
  "com.geirsson"       % "sbt-ci-release"           % "1.5.2",
  "com.typesafe"       % "sbt-mima-plugin"          % "0.3.0",
  "org.scala-js"       % "sbt-scalajs"              % "1.0.0",
  "org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0"
)

def plugins_(modules: ModuleID*) = modules.map(addSbtPlugin)
