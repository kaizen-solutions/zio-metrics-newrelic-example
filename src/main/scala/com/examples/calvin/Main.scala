package com.examples.calvin

import com.examples.calvin.metrics.*
import zio.*
import zio.logging.backend.SLF4J
import zio.metrics.Metric
import zio.metrics.jvm.DefaultJvmMetrics

object Main extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    val Micrometer =
      ZLayer
        .succeed(
          OltpMetricsConfig(
            ServiceName("cal-local-oltp-metrics"),
            NewRelicIngestLicenseKey("<Your-Key-Here>FFFFNRAL")
          )
        )
        .to(OltpMetricsRegistry.layer)

    Runtime.removeDefaultLoggers ++
      SLF4J.slf4j ++
      Runtime.enableRuntimeMetrics ++
      DefaultJvmMetrics.live ++
      Micrometer // can also be added to run if you need configuration from the environment

  val run: ZIO[ZIOAppArgs & Scope, Any, Any] = {
    val metricsCounter = Metric.counter("my_counter", "A simple counter")
    ZIO.foreachDiscard(1 to 1000)(each =>
      ZIO.sleep(1.second).as(each.toLong)
        @@ ZIOAspect.logged
        @@ metricsCounter
    )
  }
}
