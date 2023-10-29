package com.examples.calvin

import com.examples.calvin.metrics.NewRelicAgentMeterRegistry
import zio.*
import zio.logging.backend.SLF4J
import zio.metrics.Metric
object Main extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers ++ SLF4J.slf4j ++ NewRelicAgentMeterRegistry.layer

  val run =
    val metricsCounter = Metric.counter("my_counter1", "A simple counter")
    val metricsGauge   = Metric.gauge("my_gauge2", "A simple gauge").contramap[Long](_.toDouble)

    ZIO.foreachDiscard(1 to 1000)(each =>
      ZIO.sleep(1.second).as(each.toLong)
        @@ ZIOAspect.logged
        @@ metricsCounter
        @@ metricsGauge
    )
}
