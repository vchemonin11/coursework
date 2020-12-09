val circeVersion = "0.13.0"

lazy val `coursework` = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.3.3",
      "io.monix" %% "monix" % "3.2.2",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
      "com.h2database" % "h2" % "1.4.200",

      "com.typesafe.akka" %% "akka-actor" % "2.6.10",
      "com.typesafe.akka" %% "akka-stream" % "2.6.10",
      "com.typesafe.akka" %% "akka-http" % "10.2.1",
      "de.heikoseeberger" %% "akka-http-circe" % "1.35.2",

      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,

      "org.scalatest" %% "scalatest" % "3.2.0" % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.10" % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % "10.2.1" % Test,
      "org.scalamock" %% "scalamock" % "4.4.0" % Test

    ),
    scalacOptions ++= Seq("-optimize")
  )
