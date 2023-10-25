ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "zio-otel-micrometer",
    libraryDependencies ++= {
      val oltp                = "io.opentelemetry"
      val oltpInstrumentation = "io.opentelemetry.instrumentation"
      val zio                 = "dev.zio"

      val oltpV    = "1.31.0"
      val oltpInsV = "1.31.0-alpha"

      Seq(
        zio                %% "zio"                                % "2.0.18",
        zio                %% "zio-metrics-connectors-micrometer"  % "2.2.0",
        zio                %% "zio-logging-slf4j"                  % "2.1.14",
        zio                %% "zio-logging-slf4j-bridge"           % "2.1.14",
        oltp                % "opentelemetry-api"                  % oltpV,
        oltp                % "opentelemetry-sdk"                  % oltpV,
        oltp                % "opentelemetry-exporter-otlp"        % oltpV,
        oltpInstrumentation % "opentelemetry-micrometer-1.5"       % oltpInsV,
        oltpInstrumentation % "opentelemetry-logback-appender-1.0" % oltpInsV,
        "ch.qos.logback"    % "logback-classic"                    % "1.4.11"
      )
    }
  )
