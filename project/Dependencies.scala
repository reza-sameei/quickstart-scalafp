package xyz.sigmalab.projecttemplate

import sbt._
import sbt.Keys._

object Dependencies {

    object http4s {
        val org = "org.http4s"
        // val version = "0.18.22"
        val version = "0.20.0-M6"

        def apply(module : String) = org %% module % version

        val http4sBlazeServer = apply("http4s-blaze-server")
        val http4sCirce = apply("http4s-circe")
        val http4sDsl = apply("http4s-dsl")

        val http4sDefaults = Seq(http4sBlazeServer, http4sCirce, http4sDsl)
    }

    object cats {

        val org = "org.typelevel"
        val version = "1.6.0"

        val catsCore = org %% "cats-core" % version
        val catsKernel = org %% "cats-kernel" % version
        val catsMacros = org %% "cats-macros" % version

        val catsDefaults = Seq(catsCore, catsKernel, catsMacros)
    }

    object catsEffect {
        val org = "org.typelevel"
        val version = "1.2.0"
        val catsEffect = org %% "cats-effect" % version
        val catsEffectDefaults = Seq(catsEffect)
    }


    object fs2 {

        val org = "co.fs2"
        val version = "1.0.3"

        val fs2Core = org %% "fs2-core" % version
        val fs2IO = org %% "fs2-io" % version
        val fs2ReactiveStream = org %% "fs2-reactive-streams" % version

        val fs2Defaults = Seq(fs2Core, fs2IO, fs2ReactiveStream)
    }

    object doobie {
        val org = "org.tpolecat"
        val version = "0.6.0"

        val doobieCore = org %% "doobie-core" % version

        val doobieHikari = org %% "doobie-hikari" % version

        val doobieH2 = org %% "doobie-h2" % version
        val doobiePostgresql = org %% "doobie-postgres" % version

        val doobieSpecs2 = org %% "doobie-specs2" % version % Test
        val doobieScalatest = org %% "doobie-scalatest" % version % Test

        val pgsqlSetup = Seq(doobieCore, doobieHikari, doobiePostgresql)
        val h2Setup = Seq(doobieCore, doobieHikari, doobieH2)
    }

    object compilerPlugin {
        val kindProjector = "org.spire-math" %% "kind-projector" % "0.9.6"
        val betterMonadicFor = "com.olegpy" %% "better-monadic-for" % "0.2.4"
        val scalabp = "com.thesamet.scalapb" %% "compilerplugin" % "0.8.2"
    }

    object sbtPlugin {

        val partialUnification : ModuleID = "org.lyranthe.sbt" % "partial-unification" % "1.1.0"
        val sprayResolver : ModuleID = "io.spray" % "sbt-revolver" % "0.9.1"
        val nativePackager : ModuleID = "com.typesafe.sbt" % "sbt-native-packager" % "1.3.18"
        val scalabp : ModuleID = "com.thesamet" % "sbt-protoc" % "0.99.19"

        val buildInfo : ModuleID = "com.eed3si9n" % "sbt-buildinfo" % "0.9.0"
        val coursier : ModuleID = "io.get-coursier" % "sbt-coursier" % "1.1.0-M9"
        val dynver : ModuleID = "com.dwijnand" % "sbt-dynver" % "3.1.0"
        val explicitDeps : ModuleID = "com.github.cb372" % "sbt-explicit-dependencies" % "0.2.8"
        val packager : ModuleID = "com.typesafe.sbt" % "sbt-native-packager" % "1.3.15"
        val sbtHeader : ModuleID = "de.heikoseeberger" % "sbt-header" % "5.1.0"
        val scalafix : ModuleID = "ch.epfl.scala" % "sbt-scalafix" % "0.9.3"
        val scalafmt : ModuleID = "com.geirsson" % "sbt-scalafmt" % "1.6.0-RC4"
        val scalastyle : ModuleID = "org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0"
    }

    val overrides = Seq() ++
        cats.catsDefaults ++
        catsEffect.catsEffectDefaults ++
        fs2.fs2Defaults

    object test {
        val scalatest = "org.scalatest" %% "scalatest" % "3.0.5"
        val specs2 = "org.specs2" %% "specs2-core" % "4.1.0"
    }

    object logging {
        val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"
        val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
    }

}
