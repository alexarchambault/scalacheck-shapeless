plugins_(
  "com.geirsson"       % "sbt-ci-release"           % "1.3.2",
  "com.typesafe"       % "sbt-mima-plugin"          % "0.3.0",
  "org.scala-js"       % "sbt-scalajs"              % "0.6.29",
  "org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.1"
)

addSbtCoursier

def plugins_(modules: ModuleID*) = modules.map(addSbtPlugin)
