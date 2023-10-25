package com.examples.calvin.metrics

import zio.{durationInt, Duration}

opaque type ServiceName <: String = String
object ServiceName:
  def apply(value: String): ServiceName = value

opaque type NewRelicIngestLicenseKey <: String = String
object NewRelicIngestLicenseKey:
  def apply(value: String): NewRelicIngestLicenseKey = value

opaque type OLTPEndpoint <: String = String
object OLTPEndpoint:
  def apply(value: String): OLTPEndpoint = value

final case class OltpMetricsConfig(
  serviceName: ServiceName,
  newRelicIngestLicenseKey: NewRelicIngestLicenseKey,
  oltpEndpoint: OLTPEndpoint = OLTPEndpoint("https://otlp.nr-data.net:4317"),
  interval: Duration = 30.seconds
)
