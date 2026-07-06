package io.iot.sensor.ingestion.routes.consumer.rest;

import static io.iot.sensor.ingestion.constants.Constants.APPLICATION_JSON;
import static io.iot.sensor.ingestion.constants.Constants.ORIGINAL_ACCEPT_CONTENT_TYPE;
import static io.iot.sensor.ingestion.constants.Constants.ORIGINAL_CONTENT_TYPE;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iot.sensor.ingestion.exception.GenericExceptionHandler;
import io.iot.sensor.ingestion.mapper.api.JsonSensorReadingMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.camelbee.config.CamelBeeRouteConfigurer;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("PMD.TooManyStaticImports")
public class RestConsumerRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
  final GenericExceptionHandler genericExceptionHandler;
  final ObjectMapper objectMapper;
  final JsonSensorReadingMapper jsonSensorReadingMapper;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(genericExceptionHandler.appErrorHandler());

    JacksonDataFormat dataFormat = new JacksonDataFormat();
    dataFormat.setObjectMapper(objectMapper);

    restConfiguration().bindingMode(RestBindingMode.off);

    rest().openApi().specification("openapi/sensor-api.yaml").missingOperation("ignore");

    from("direct:listSensorReadings")
        .routeId("listSensorReadingsRoute")
        .process(this::setOriginalContentTypeProperties)
        .to("direct:centralListReadings")
        .bean(jsonSensorReadingMapper, "domainToApiSensorReadingPage")
        .marshal(dataFormat)
        .process(this::setOriginalContentTypePropertiesBack)
        .setHeader(HTTP_RESPONSE_CODE, constant(200));

    from("direct:getSensorReading")
        .routeId("getSensorReadingOperationRoute")
        .process(this::setOriginalContentTypeProperties)
        .to("direct:centralGetReading")
        .bean(jsonSensorReadingMapper, "domainToApiSensorReading")
        .marshal(dataFormat)
        .process(this::setOriginalContentTypePropertiesBack)
        .setHeader(HTTP_RESPONSE_CODE, constant(200));

  }

  private void setOriginalContentTypeProperties(Exchange exchange) {
    Message message = exchange.getIn();
    String contentType = message.getHeader(CONTENT_TYPE, String.class);
    if (contentType == null) {
      contentType = APPLICATION_JSON;
    }
    exchange.setProperty(ORIGINAL_CONTENT_TYPE, contentType);
    exchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");
    String acceptHeader = message.getHeader(HttpHeaders.ACCEPT, String.class);
    if (acceptHeader == null || acceptHeader.isEmpty() || "*/*".equals(acceptHeader)) {
      exchange.setProperty(ORIGINAL_ACCEPT_CONTENT_TYPE, contentType);
    } else {
      exchange.setProperty(ORIGINAL_ACCEPT_CONTENT_TYPE, acceptHeader);
    }
  }

  private void setOriginalContentTypePropertiesBack(Exchange exchange) {
    if (exchange.getProperty(ORIGINAL_ACCEPT_CONTENT_TYPE) != null) {
      exchange.getIn().setHeader(CONTENT_TYPE, exchange.getProperty(ORIGINAL_ACCEPT_CONTENT_TYPE));
    }
    exchange.getIn().removeHeader(HttpHeaders.ACCEPT);
  }
}
