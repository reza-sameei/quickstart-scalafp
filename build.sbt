
/*
lazy val root = (project in file("."))
  .enablePlugins(ProjectPlugin).aggregate(webapi)
*/


import xyz.sigmalab.projecttemplate._

lazy val todoModel = Project("todoModel", file("todo/model"))
    .enablePlugins(ProjectPlugin)
    .settings(
        libraryDependencies ++= Dependencies.cats.catsDefaults,
        libraryDependencies ++= Dependencies.catsEffect.catsEffectDefaults,
        // libraryDependencies ++= Dependencies.fs2.fs2,
        libraryDependencies ++= Dependencies.doobie.pgsqlSetup,
        libraryDependencies += Dependencies.doobie.doobieScalatest % Test,
        libraryDependencies += Dependencies.logging.logback % Test
    )

lazy val todoWebAPI = Project("todoWebAPI", file("todo/webapi"))
    .enablePlugins(ProjectPlugin, JavaAppPackaging)
    .dependsOn(todoModel)
    .settings(
        libraryDependencies ++= Dependencies.http4s.http4sDefaults,
        libraryDependencies ++= Dependencies.circe.circeDefaults,
        libraryDependencies += Dependencies.logging.logback,
        libraryDependencies += Dependencies.test.scalatest % Test,
    )

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
    libraryDependencies += Dependencies.doobie.doobieScalatest,
    libraryDependencies += Dependencies.test.scalatest,
    libraryDependencies += Dependencies.test.specs2,
    libraryDependencies += Dependencies.logging.logback
  )
