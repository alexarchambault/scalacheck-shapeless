plugins_(
  "com.typesafe"       % "sbt-mima-plugin"          % "0.3.0",
  "com.jsuereth"       % "sbt-pgp"                  % "1.1.1",
  "org.scala-js"       % "sbt-scalajs"              % "0.6.23",
  "org.portable-scala" % "sbt-scalajs-crossproject" % "0.5.0",
  "com.dwijnand"       % "sbt-travisci"             % "1.1.1"
)

addSbtCoursier

def plugins_(modules: ModuleID*) = modules.map(addSbtPlugin)
