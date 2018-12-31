plugins_(
  "com.geirsson"       % "sbt-ci-release"           % "1.2.1",
  "com.typesafe"       % "sbt-mima-plugin"          % "0.3.0",
  "com.jsuereth"       % "sbt-pgp"                  % "1.1.2",
  "org.scala-js"       % "sbt-scalajs"              % "0.6.26",
  "org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.0",
  "com.dwijnand"       % "sbt-travisci"             % "1.1.3"
)

addSbtCoursier

def plugins_(modules: ModuleID*) = modules.map(addSbtPlugin)
