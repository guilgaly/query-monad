// Resolvers
resolvers ++= Seq(
  "Entrepot Releases" at "https://raw.github.com/zengularity/entrepot/master/releases",
  "Entrepot Snapshots" at "https://raw.github.com/zengularity/entrepot/master/snapshots")

// Plugins
val neoScalafmtVersion = "1.14"
addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % neoScalafmtVersion)
// addSbtPlugin("com.lucidchart" % "sbt-scalafmt-coursier" % neoScalafmtVersion)

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.10")

// addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC13")

addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.2.1")
