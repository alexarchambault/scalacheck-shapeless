plugins_(
  "com.geirsson"       % "sbt-ci-release"           % "1.2.6",
  "com.typesafe"       % "sbt-mima-plugin"          % "0.6.0",
  "com.jsuereth"       % "sbt-pgp"                  % "1.1.2",
  "org.scala-js"       % "sbt-scalajs"              % "0.6.28",
  "org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.1"
)

addSbtCoursier

def plugins_(modules: ModuleID*) = modules.map(addSbtPlugin)
