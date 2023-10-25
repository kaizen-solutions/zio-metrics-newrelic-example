package com.examples.calvin.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.instrumentation.micrometer.v1_5.OpenTelemetryMeterRegistry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.`export`.{
  AggregationTemporalitySelector,
  DefaultAggregationSelector,
  PeriodicMetricReader
}
import io.opentelemetry.sdk.metrics.{Aggregation, InstrumentType, SdkMeterProvider}
import io.opentelemetry.sdk.resources.Resource as OltpResource
import zio.*
import zio.metrics.connectors.micrometer.*

object OltpMetricsRegistry:
  private val meterRegistryLayer: RLayer[OltpMetricsConfig, MeterRegistry] =
    ZLayer.scoped(
      for {
        config <- ZIO.service[OltpMetricsConfig]
        registry <- openTelemetryMeterRegistry(
                      config.serviceName,
                      config.newRelicIngestLicenseKey,
                      config.oltpEndpoint,
                      config.interval
                    )
      } yield registry
    )

  val layer: RLayer[OltpMetricsConfig, MeterRegistry] =
    ZLayer
      .succeed(MicrometerConfig.default)
      .and(meterRegistryLayer)
      .andTo(micrometerLayer)

  private def openTelemetryMeterRegistry(
    serviceName: ServiceName,
    newRelicLicenseKey: NewRelicIngestLicenseKey,
    oltpEndpoint: OLTPEndpoint,
    interval: Duration
  ): RIO[Scope, MeterRegistry] =
    openTelemetry(serviceName, newRelicLicenseKey, oltpEndpoint, interval).flatMap { sdk =>
      ZIO.acquireRelease(
        ZIO.attempt(
          OpenTelemetryMeterRegistry
            .builder(sdk)
            .build()
        )
      )(registry => ZIO.succeed(registry.close()))
    }

  private def openTelemetry(
    serviceName: ServiceName,
    newRelicLicenseKey: NewRelicIngestLicenseKey,
    oltpEndpoint: OLTPEndpoint,
    interval: Duration
  ): RIO[Scope, OpenTelemetrySdk] =
    meterProvider(serviceName, newRelicLicenseKey, oltpEndpoint, interval).flatMap { meterProvider =>
      ZIO.acquireRelease(
        ZIO.attempt(
          OpenTelemetrySdk
            .builder()
            .setMeterProvider(meterProvider)
            .build()
        )
      )(sdk => ZIO.succeed(sdk.shutdown()))
    }

  // https://docs.newrelic.com/docs/more-integrations/open-source-telemetry-integrations/opentelemetry/get-started/opentelemetry-set-up-your-app/
  private def meterProvider(
    serviceName: ServiceName,
    newRelicLicenseKey: NewRelicIngestLicenseKey,
    oltpEndpoint: OLTPEndpoint,
    interval: Duration
  ): RIO[Scope, SdkMeterProvider] =
    metricReader(newRelicLicenseKey, oltpEndpoint, interval).flatMap { reader =>
      ZIO.acquireRelease(
        ZIO.attempt(
          SdkMeterProvider
            .builder()
            .setResource(oltpResource(serviceName))
            .registerMetricReader(reader)
            .build()
        )
      )(provider => ZIO.succeed(provider.shutdown()))
    }

  private def oltpResource(serviceName: ServiceName): OltpResource =
    OltpResource
      .builder()
      .put("service.name", serviceName)
      .put("service", serviceName)
      // Include instrumentation.provider=micrometer to enable micrometer metrics in New Relic
      .put("instrumentation.provider", "micrometer")
      .build()

  private def metricReader(
    newRelicLicenseKey: NewRelicIngestLicenseKey,
    oltpEndpoint: OLTPEndpoint,
    interval: Duration
  ): RIO[Scope, PeriodicMetricReader] = {
    def oltpMetricExporter(newRelicLicenseKey: NewRelicIngestLicenseKey): RIO[Scope, OtlpGrpcMetricExporter] =
      ZIO.acquireRelease(
        ZIO.attempt(
          OtlpGrpcMetricExporter
            .builder()
            .setEndpoint(oltpEndpoint)
            .addHeader("api-key", newRelicLicenseKey)
            // IMPORTANT: New Relic requires metrics to be delta temporality
            .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
            // Use exponential histogram aggregation for histogram instruments
            // to produce better data and compression
            .setDefaultAggregationSelector(
              DefaultAggregationSelector.getDefault
                .`with`(InstrumentType.HISTOGRAM, Aggregation.base2ExponentialBucketHistogram())
            )
            .build()
        )
      )(exporter => ZIO.succeed(exporter.shutdown()))

    oltpMetricExporter(newRelicLicenseKey).flatMap { exporter =>
      ZIO.acquireRelease(
        ZIO.attempt(
          PeriodicMetricReader
            .builder(exporter)
            .setInterval(interval)
            .build()
        )
      )(reader => ZIO.succeed(reader.shutdown()))
    }
  }
