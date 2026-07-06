package io.iot.sensor.ingestion.routes.central;

import io.iot.sensor.ingestion.constants.Constants;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CentralListReadingsRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralListReadings").routeId("centralListReadingsRoute")
        .setProperty(Constants.ORIGINAL_BODY, body())
        .process(this::validateDeviceId)
        .to("direct:listReadingsMongoDb").id("listReadingsMongoDbEndpoint");
  }

  /**
   * Validates that the deviceId header is present and non-blank.
   * The deviceId is a required query parameter per the OpenAPI spec.
   */
  @SneakyThrows
  private void validateDeviceId(Exchange exchange) {
    String deviceId = exchange.getIn().getHeader("deviceId", String.class);
    if (deviceId == null || deviceId.isBlank()) {
      throw new ValidationException(exchange, "deviceId is required");
    }
  }
}
