# ZIO Metrics Micrometer New Relic Example

This is an example of how you can use ZIO Metrics Connectors (Micrometer) bridged to the New Relic Java Agent to send 
metrics to New Relic via custom events.

## Running the example
Set the following environment variables
* `NEW_RELIC_LICENSE_KEY` to your New Relic license key
* `NEW_RELIC_APP_NAME` to the name of your New Relic application

Alternatively, you can set the equivalent settings in the `newrelic.yml` file in the root of the project. You can also 
enable audit mode in the `newrelic.yml` file to see the metrics being sent to New Relic.

Then run the example with `sbt run`

Confirm that the New Relic agent is running by checking the logs for "New Relic Agent Enabled"

<img width="1119" alt="image" src="https://github.com/kaizen-solutions/zio-metrics-newrelic-example/assets/14280155/9ecca22d-160a-4507-b16e-aff603318fec">


This requires a New Relic Ingest License Key.
