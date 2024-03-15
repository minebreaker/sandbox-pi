ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.4.0"

lazy val root = (project in file("."))
  .settings(
    name := "sandbox-pi",
    libraryDependencies := Seq(
      // Core
      "org.eclipse.jetty" % "jetty-server" % "11.0.14",
      "com.google.guava" % "guava" % "33.0.0-jre",
      "com.google.inject" % "guice" % "7.0.0",
      "com.pi4j" % "pi4j-plugin-raspberrypi" % "2.4.0",
      "com.pi4j" % "pi4j-plugin-pigpio" % "2.4.0",
//      "com.pi4j" % "pi4j-plugin-linuxfs" % "2.4.0",
      "com.squareup.moshi" % "moshi" % "1.15.0",
      "com.typesafe" % "config" % "1.4.2",
      "ch.qos.logback" % "logback-classic" % "1.4.14",
      // Scala thing
      "org.typelevel" %% "cats-effect" % "3.5.3",
      "io.scalaland" %% "chimney" % "0.8.0"
    ) ++ Seq(
      "org.scalatest" %% "scalatest-funspec" % "3.2.17",
      "org.scalatestplus" %% "mockito-4-11" % "3.2.17.0"
    ).map(_ % Test),
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest),
    Test / fork := true,
    Test / testForkedParallel := true,

    assemblyMergeStrategy := {
      case "module-info.class" => MergeStrategy.discard
      case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
      case v => (assembly / assemblyMergeStrategy).value.apply(v)
    }
  )
