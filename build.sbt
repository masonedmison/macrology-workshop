val scala2 = "2.13.8"
ThisBuild / scalaVersion := scala2
val scalatestVersion = "3.2.12"

lazy val macros = (project in file("macros")).settings(
  scalacOptions -= "-Xfatal-warnings",
  scalacOptions ++= Seq(
    // "-Ymacro-annotations",
    "-optimize",
    "-language:experimental.macros",
    "-optimize",
    "-Xprint:typer",
    "-Xprint-types"
  ),
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scala2,
  )
)

lazy val core = (project in file("core"))
  .settings(
    libraryDependencies += "org.scalatest"  %% "scalatest"    % scalatestVersion % Test
  )
  .dependsOn(macros)
