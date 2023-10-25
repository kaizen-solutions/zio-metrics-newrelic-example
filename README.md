# ZIO Metrics Micrometer New Relic Example

This is an example of how you can use ZIO Metrics Connectors (Micrometer) bridged to OpenTelemetry to send metrics to 
New Relic by utilizing their [OpenTelemetry gRPC endpoint](https://docs.newrelic.com/docs/more-integrations/open-source-telemetry-integrations/opentelemetry/get-started/opentelemetry-set-up-your-app).

This requires a New Relic Ingest License Key.

Adapted from the [New Relic Micrometer shim example](https://github.com/newrelic/newrelic-opentelemetry-examples/tree/main/other-examples/java/micrometer-shim)