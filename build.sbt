import sbt.Keys.{scalaVersion, version}


lazy val `scala-fintech-school` = project
  .in(file("."))
  .settings(
    name in ThisBuild := "scala-global-2020",
    version in ThisBuild := "0.1",
    scalaVersion in ThisBuild := "2.13.3",

  )
  .aggregate(
    `coursework`
  )

lazy val `coursework` = project
