package io.iot.sensor.ingestion.routes.central;

import io.iot.sensor.ingestion.constants.Constants;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CentralIngestReadingRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralIngestReading").routeId("centralIngestReadingRoute")
        .setProperty(Constants.ORIGINAL_BODY, body())
        .to("direct:ingestReadingMongoDb").id("ingestReadingMongoDbEndpoint")
        .to("direct:ingestReadingKafka").id("ingestReadingKafkaEndpoint")
        .setBody(exchangeProperty(Constants.ACTUAL_RESPONSE_BODY));
  }
}
