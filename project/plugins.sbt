resolvers += Resolver.sonatypeRepo("staging")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.7")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.9")
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-M12")
