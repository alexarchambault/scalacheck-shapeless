plugins_(
  "io.get-coursier"  % "sbt-coursier"             % "1.0.0-RC6",
  "org.scala-native" % "sbt-crossproject"         % "0.2.0",
  "com.typesafe"     % "sbt-mima-plugin"          % "0.1.14",
  "com.jsuereth"     % "sbt-pgp"                  % "1.0.1",
  "org.scala-js"     % "sbt-scalajs"              % "0.6.18",
  "org.scala-native" % "sbt-scalajs-crossproject" % "0.2.0",
  "com.dwijnand"     % "sbt-travisci"             % "1.1.0"
)

def plugins_(modules: ModuleID*) = modules.map(addSbtPlugin)
