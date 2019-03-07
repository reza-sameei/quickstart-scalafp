package xyz.sigmalab.projecttemplate

import sbt._
import sbt.Keys._

object ProjectPlugin extends AutoPlugin {
    
    val rootArtifactSettings = Seq( 
        scalaVersion := "2.12.8",
        organization := "xyz.sigmalab.portland",
        version := "0.1.0-SNAPSHOT"
    )

    /**
      * @see https://docs.scala-lang.org/overviews/compiler-options/index.html
      */
    private lazy val optOptions: Seq[String] =
        if (sys.env.contains("HELM_VERSION")) {
            Seq(
                "-opt:l:inline",
                "-opt:l:method",
                "-opt-inline-from:**",
                "-opt:unreachable-code",
                "-opt:simplify-jumps",
                "-opt:compact-locals",
                "-opt:copy-propagation",
                "-opt:redundant-casts",
                "-opt:box-unbox",
                "-opt:nullness-tracking",
                "-opt:closure-invocations",
                "-Yopt-log-inline"
            )
        } else {
            Seq.empty
        }

    private lazy val scalacOptionsFor212: Seq[String] =
        Seq(
            "-Xlint:constant",
            "-Ywarn-extra-implicit",
            "-Ywarn-unused:implicits",
            "-Ywarn-unused:imports",
            "-Ywarn-unused:locals",
            "-Ywarn-unused:params",
            "-Ywarn-unused:patvars",
            "-Ywarn-unused:privates"
        ) ++ optOptions

    private lazy val commonScalacOptions: Seq[String] =
        Seq(
            "-deprecation",
            "-encoding",
            "utf-8",
            "-explaintypes",
            "-feature",
            "-language:existentials",
            "-language:higherKinds",
            "-language:implicitConversions",
            "-unchecked",
            "-Xfuture",
            "-Xlint:adapted-args",
            "-Xlint:by-name-right-associative",
            "-Xlint:delayedinit-select",
            "-Xlint:doc-detached",
            "-Xlint:inaccessible",
            "-Xlint:infer-any",
            "-Xlint:missing-interpolator",
            "-Xlint:nullary-override",
            "-Xlint:nullary-unit",
            "-Xlint:option-implicit",
            "-Xlint:package-object-classes",
            "-Xlint:poly-implicit-overload",
            "-Xlint:private-shadow",
            "-Xlint:stars-align",
            "-Xlint:type-parameter-shadow",
            "-Xlint:unsound-match",
            "-Yno-adapted-args",
            "-Ypartial-unification",
            "-Yrangepos",
            "-Ywarn-dead-code",
            "-Ywarn-inaccessible",
            "-Ywarn-infer-any",
            "-Ywarn-nullary-override",
            "-Ywarn-nullary-unit",
            "-Ywarn-numeric-widen",
            "-Ywarn-value-discard"
        )

    val commonOptions = Seq(
            scalacOptions ++= commonScalacOptions ++ scalacOptionsFor212
        ,   Test / fork := true
        ,   Compile / fork := true
        ,   run / connectInput := true
        ,   outputStrategy := Some(StdoutOutput)
        //,   Test / run / javaOptions += "-Xmx8G"
        //,   Test / javaOptions += "-Xmx8G"
        //,   run / javaHome := Some(file("???"))
    )

    val compilerPluginsSettings = Seq(
        addCompilerPlugin(Dependencies.compilerPlugin.kindProjector),
        addCompilerPlugin(Dependencies.compilerPlugin.betterMonadicFor),
        addCompilerPlugin(Dependencies.compilerPlugin.scalabp)
    )

    val dependencyManagement = Seq(
        dependencyOverrides ++= Dependencies.overrides
    )

    override val projectSettings = 
        compilerPluginsSettings ++
            commonOptions ++
            dependencyManagement

}
