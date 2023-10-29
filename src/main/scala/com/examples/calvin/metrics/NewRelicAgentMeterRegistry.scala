package com.examples.calvin.metrics

import com.newrelic.api.agent.{Agent, NewRelic as NewRelicAgent}
import io.micrometer.core.instrument.{MeterRegistry, Tag}
import io.micrometer.newrelic.*
import zio.metrics.connectors.micrometer.{MicrometerConfig, micrometerLayer}
import zio.{Tag as _, *}

import scala.jdk.CollectionConverters.*

final case class NewRelicApplicationConfig(
  applicationName: String,
  tags: Map[String, String]
):
  def toMicroMeterTags: java.util.List[Tag] =
    tags.updated("service", applicationName).map { case (k, v) => Tag.of(k, v) }.toList.asJava

// NOTE: This publishes metrics as custom events not metrics (utilizing the Java Agent)
object NewRelicAgentMeterRegistry {
  private val agent: ULayer[Agent] =
    ZLayer(
      ZIO.succeed(NewRelicAgent.getAgent).tap { agent =>
        val agentEnabled: Boolean = !agent.toString.toLowerCase.contains("noopagent")
        if agentEnabled then ZIO.logInfo("New Relic Agent Enabled")
        else ZIO.logInfo("New Relic Agent Disabled")
      }
    )

  private val newRelicConfig: ULayer[NewRelicConfig] =
    ZLayer.succeed(new NewRelicConfig:
      override def get(key: String): String = null
      
      /**
       * false means you search using: `select * from MicrometerSample where
       * metricName = 'my_gauge2' and metricType = 'GAUGE' and appName =
       * 'CalvinTestMicrometerNewRelic' since 10 minutes ago`
       *
       * true means you search using `SELECT * FROM 'my_gauge2'`
       * @return
       */
      override def meterNameEventTypeEnabled(): Boolean = false
    )

  private val agentConfig: URLayer[NewRelicConfig & Agent, NewRelicInsightsAgentClientProvider] =
    ZLayer.fromFunction(new NewRelicInsightsAgentClientProvider(_, _))

  private val meterRegistry: URLayer[
    NewRelicApplicationConfig & NewRelicConfig & NewRelicInsightsAgentClientProvider,
    NewRelicMeterRegistry & MeterRegistry
  ] =
    ZLayer(
      for
        appConfig <- ZIO.service[NewRelicApplicationConfig]
        config    <- ZIO.service[NewRelicConfig]
        provider  <- ZIO.service[NewRelicInsightsAgentClientProvider]
        registry <- ZIO.succeed(
                      NewRelicMeterRegistry
                        .builder(config)
                        .clientProvider(provider)
                        .build()
                    )
        _ <- ZIO.succeed(registry.config().commonTags(appConfig.toMicroMeterTags))
      yield registry
    )

  private val configuredMeterRegistry: URLayer[NewRelicApplicationConfig, NewRelicMeterRegistry & MeterRegistry] =
    ZLayer.makeSome[NewRelicApplicationConfig, NewRelicMeterRegistry & MeterRegistry](
      agent,
      newRelicConfig,
      agentConfig,
      meterRegistry
    )

  val layer: ULayer[MeterRegistry] =
    ZLayer
      .make[NewRelicMeterRegistry](
        ZLayer.succeed(NewRelicApplicationConfig("calvin", Map.empty)),
        configuredMeterRegistry,
        ZLayer.succeed(MicrometerConfig.default),
        micrometerLayer
      )
}
