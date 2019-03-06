
// import xyz.sigmalab.projecttemplate.Dependencies

/*addSbtPlugin(Dependencies.sbtPlugin.partialUnification)
//addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.3")
addSbtPlugin(Dependencies.sbtPlugin.sprayResolver)
addSbtPlugin(Dependencies.sbtPlugin.nativePackager)
addSbtPlugin(Dependencies.sbtPlugin.scalabp)
addSbtPlugin(Dependencies.sbtPlugin.buildInfo)
addSbtPlugin(Dependencies.sbtPlugin.coursier)
addSbtPlugin(Dependencies.sbtPlugin.explicitDeps)*/

// dependencyOverrides := xyz.sigmalab.projecttemplate.Dependencies.overrides

addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.0")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
addSbtPlugin( "com.typesafe.sbt" % "sbt-native-packager" % "1.3.18")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.19")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.1.0-M9")
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.2.8")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.1.0")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.3")
addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.6.0-RC4")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")