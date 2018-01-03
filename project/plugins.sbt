plugins_(
  "com.typesafe"       % "sbt-mima-plugin"          % "0.1.18",
  "com.jsuereth"       % "sbt-pgp"                  % "1.1.0",
  "org.scala-js"       % "sbt-scalajs"              % "0.6.21",
  "org.portable-scala" % "sbt-scalajs-crossproject" % "0.3.0",
  "com.dwijnand"       % "sbt-travisci"             % "1.1.1"
)

addSbtCoursier

def plugins_(modules: ModuleID*) = modules.map(addSbtPlugin)
