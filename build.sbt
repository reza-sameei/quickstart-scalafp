
/*
lazy val root = (project in file("."))
  .enablePlugins(ProjectPlugin).aggregate(webapi)
*/


import xyz.sigmalab.projecttemplate._

lazy val webapi = Project("webapi", file("webapi")) // (project in file(""))
  .enablePlugins(ProjectPlugin, JavaAppPackaging)
  .settings(
    name := "portland-webapi",
    libraryDependencies ++= Dependencies.http4s.http4sDefaults,
    libraryDependencies ++= Dependencies.cats.catsDefaults,
    libraryDependencies ++= Dependencies.catsEffect.catsEffectDefaults,
    // libraryDependencies ++= Dependencies.fs2.fs2,
    libraryDependencies ++= Dependencies.doobie.pgsqlSetup,
    libraryDependencies += Dependencies.doobie.doobieSpecs2,
    libraryDependencies += Dependencies.test.scalatest,
    libraryDependencies += Dependencies.test.specs2,
    libraryDependencies += Dependencies.logging.logback
  )
