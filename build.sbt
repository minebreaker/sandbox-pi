ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.4.0"

lazy val root = (project in file("."))
  .aggregate(backend, frontend)

val scalaJsSettings = scalaJSLinkerConfig ~= {
  _.withModuleKind(ModuleKind.ESModule)
    .withModuleSplitStyle(
      org.scalajs.linker.interface.ModuleSplitStyle.SmallModulesFor(List("livechart"))
    )
}

lazy val shared = (project in file("shared"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJsSettings
  )

lazy val backend = (project in file("backend"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "backend",
    organization := "rip.deadcode",
    libraryDependencies := Seq(
      // Core
      "org.eclipse.jetty" % "jetty-server" % "11.0.14",
      "com.google.guava" % "guava" % "33.0.0-jre",
      "com.google.inject" % "guice" % "7.0.0",
      "com.pi4j" % "pi4j-plugin-raspberrypi" % "2.4.0",
      "com.pi4j" % "pi4j-plugin-pigpio" % "2.4.0",
      //      "com.pi4j" % "pi4j-plugin-linuxfs" % "2.4.0",
      "com.typesafe" % "config" % "1.4.3",
      "ch.qos.logback" % "logback-classic" % "1.4.14",
      "io.circe" %% "circe-core" % "0.15.0-M1",
      "io.circe" %% "circe-parser" % "0.15.0-M1",
      "io.circe" %% "circe-generic" % "0.15.0-M1",
      // Database
      "org.jdbi" % "jdbi3-core" % "3.43.0",
      "org.postgresql" % "postgresql" % "42.7.3",
      "com.zaxxer" % "HikariCP" % "5.1.0",
      "org.flywaydb" % "flyway-core" % "9.22.3",
      // Scala thing
      "org.typelevel" %% "cats-effect" % "3.5.3",
      "io.scalaland" %% "chimney" % "0.8.5",
      // Sttp
      "com.softwaremill.sttp.client3" %% "core" % "3.9.5",
      "com.softwaremill.sttp.client3" %% "circe" % "3.9.5",
      "com.softwaremill.sttp.client3" %% "cats" % "3.9.5"
    ) ++ Seq(
      "org.scalatest" %% "scalatest-funspec" % "3.2.17",
      "org.scalatestplus" %% "mockito-4-11" % "3.2.17.0"
    ).map(_ % Test),
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest),
    Test / fork := true,
    Test / testForkedParallel := true,

    // Buildinfo
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "rip.deadcode.sandbox_pi.build_info",

    // Docker
    Docker / dockerBaseImage := "amazoncorretto:21-alpine",
    Docker / packageName := "sandbox-pi",
    Docker / dockerExposedPorts := Seq(8080),
    Docker / dockerBuildxPlatforms := Seq("linux/arm64"),
    // Fat jar
    assemblyMergeStrategy := {
      case "module-info.class"                                        => MergeStrategy.discard
      case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
      case v => (assembly / assemblyMergeStrategy).value.apply(v)
    }
  )

lazy val frontend = (project in file("frontend"))
  .enablePlugins(
    ScalaJSPlugin,
    ScalablyTypedConverterExternalNpmPlugin
  )
  .settings(
    name := "frontend",
    scalaJSUseMainModuleInitializer := true,
    mainClass := Some("rip.deadcode.sandbox_pi.frontend.Main"),
    stFlavour := Flavour.Slinky,
    externalNpm := {
      scala.sys.process.Process("npm", baseDirectory.value).!
      baseDirectory.value
    },
    scalaJsSettings,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.4.0",
      "me.shadaj" %%% "slinky-core" % "0.7.3",
      "me.shadaj" %%% "slinky-web" % "0.7.3"
    )
  )
  .dependsOn(shared % "test->test;compile->compile")
