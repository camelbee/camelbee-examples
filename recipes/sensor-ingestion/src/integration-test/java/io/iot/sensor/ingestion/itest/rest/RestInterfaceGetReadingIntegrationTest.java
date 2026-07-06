package io.iot.sensor.ingestion.itest.rest;

import static io.iot.sensor.ingestion.constants.Constants.APPLICATION_JSON;
import static io.iot.sensor.ingestion.utils.testdata.SensorReadingTestDataProducer.RequestScenarios;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.iot.sensor.ingestion.itest.GetReadingIntegrationTest;
import io.iot.sensor.ingestion.itest.IntegrationTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class RestInterfaceGetReadingIntegrationTest extends GetReadingIntegrationTest {

  private static final Logger LOG = LoggerFactory.getLogger(RestInterfaceGetReadingIntegrationTest.class);

  static final String SENSOR_BASE_URL = "/camelbee-service/sensor-readings";

  Exchange exchange;
  Map<String, Object> defaultHeaders;

  @BeforeAll
  public void setup() throws Exception {
    var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);
    var routeDefinition = new RouteDefinition();
    routeDefinition.from("direct:testRestGetReading")
        .toD("http:localhost:{{quarkus.http.test-port}}"
            + SENSOR_BASE_URL + "/${header.readingId}?throwExceptionOnFailure=false");
    modelContext.addRouteDefinition(routeDefinition);

    defaultHeaders = Map.of(
        HttpHeaders.ACCEPT, "*/*",
        "CamelHttpMethod", "GET",
        "CamelHttpCharacterEncoding", "UTF-8");

    super.setup();

    int docCount = countMongodbCollection("sensorReadings");
    LOG.info("MongoDB sensorReadings count after resetBeforeAll: {}", docCount);
  }

  @BeforeEach
  public void setupEndpoints() throws Exception {
    exchange = ExchangeBuilder.anExchange(camelContext).build();
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("getSuccessParameters")
  void given_ValidReadingId_When_CallingDirectGetSensorReading_Then_ResultIsSuccess(
      String fileName, String readingId, String contentType, int httpStatus) throws Exception {
    setupGetSuccessScenario();

    Exchange directExchange = ExchangeBuilder.anExchange(camelContext).build();
    directExchange.getIn().setHeader("readingId", readingId);
    directExchange.getIn().setBody(null);
    var result = fluentProducerTemplate
        .to("direct:getSensorReading")
        .withExchange(directExchange)
        .send();
    var statusCode = result.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
    var responseBody = result.getMessage().getBody(String.class);
    LOG.info("Direct getSensorReading: status={}, body='{}', headers={}",
        statusCode, responseBody, result.getMessage().getHeaders());
    assertThat(statusCode).isEqualTo(httpStatus);
    validateGetSuccessScenario();
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("getSuccessParameters")
  void given_ValidReadingId_When_GetRouteCalled_Then_ResultIsSuccess(
      String fileName, String readingId, String contentType, int httpStatus) throws Exception {
    setupGetSuccessScenario();
    var result = callTestRoute(readingId, contentType);
    var resultMsg = result.getMessage();
    var statusCode = resultMsg.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
    var responseBody = resultMsg.getBody(String.class);
    LOG.info("GetReading HTTP response: status={}, body='{}'", statusCode, responseBody);
    LOG.info("Response headers: {}", resultMsg.getHeaders());
    assertThat(statusCode).isEqualTo(httpStatus);
    validateGetSuccessScenario();
  }

  private Exchange callTestRoute(String readingId, String contentType) throws Exception {
    Map<String, Object> headers = new HashMap<>(defaultHeaders);
    headers.put("readingId", readingId);
    headers.put(HttpHeaders.ACCEPT, contentType);
    exchange.getIn().setHeaders(headers);
    exchange.getIn().setBody(null);
    return fluentProducerTemplate
        .to("direct:testRestGetReading")
        .withExchange(exchange)
        .send();
  }

  private static Stream<Arguments> getSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.GET_READING_SUCCESS, "test-reading-001", APPLICATION_JSON, Status.OK.getStatusCode())
    );
  }
}
