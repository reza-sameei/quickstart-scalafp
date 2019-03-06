package xyz.sigmalab.projecttemplate

import sbt._
import sbt.Keys._

object ProjectPlugin extends AutoPlugin {
    
    val rootArtifactSettings = Seq( 
        scalaVersion := "2.12.8",
        organization := "xyz.sigmalab.portland",
        version := "0.1.0-SNAPSHOT"
    )

    val commonOptions = Seq(
            scalacOptions += "-Ypartial-unification"
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
