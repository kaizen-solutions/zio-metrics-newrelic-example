ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .enablePlugins(JavaAgent)
  .settings(javaAgents += "com.newrelic.agent.java" % "newrelic-agent" % "8.7.0" % "compile")
  .settings(
    name := "zio-newrelic-micrometer",
    libraryDependencies ++= {
      val zio = "dev.zio"

      Seq(
        "com.newrelic.agent.java" % "newrelic-api"                      % "8.6.0",
        "io.micrometer"           % "micrometer-registry-new-relic"     % "1.11.5",
        zio                      %% "zio"                               % "2.0.18",
        zio                      %% "zio-metrics-connectors-micrometer" % "2.2.0",
        zio                      %% "zio-logging-slf4j"                 % "2.1.14",
        zio                      %% "zio-logging-slf4j-bridge"          % "2.1.14",
        "ch.qos.logback"          % "logback-classic"                   % "1.4.11"
      )
    }
  )
