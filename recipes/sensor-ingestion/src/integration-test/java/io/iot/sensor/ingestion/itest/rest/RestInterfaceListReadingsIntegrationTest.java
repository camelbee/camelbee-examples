package io.iot.sensor.ingestion.itest.rest;

import static io.iot.sensor.ingestion.constants.Constants.APPLICATION_JSON;
import static io.iot.sensor.ingestion.utils.testdata.SensorReadingTestDataProducer.RequestScenarios;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.iot.sensor.ingestion.itest.IntegrationTestProfile;
import io.iot.sensor.ingestion.itest.ListReadingsIntegrationTest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class RestInterfaceListReadingsIntegrationTest extends ListReadingsIntegrationTest {

  static final String SENSOR_BASE_URL = "/camelbee-service/sensor-readings";

  Exchange exchange;
  Map<String, Object> defaultHeaders;

  @BeforeAll
  public void setup() throws Exception {
    var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);
    var routeDefinition = new RouteDefinition();
    routeDefinition.from("direct:testRestListReadings").to(
        "http:localhost:{{quarkus.http.test-port}}" + SENSOR_BASE_URL + "?throwExceptionOnFailure=false");
    modelContext.addRouteDefinition(routeDefinition);

    defaultHeaders = Map.of(
        HttpHeaders.ACCEPT, "*/*",
        "CamelHttpMethod", "GET",
        "CamelHttpCharacterEncoding", "UTF-8");
    super.setup();
  }

  @BeforeEach
  public void setupEndpoints() throws Exception {
    exchange = ExchangeBuilder.anExchange(camelContext).build();
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("listSuccessParameters")
  void given_ValidDeviceId_When_ListRouteCalled_Then_ResultIsSuccess(
      String fileName, String deviceId, String page, String pageSize, String contentType, int httpStatus) throws Exception {
    setupListSuccessScenario();
    var result = callTestRoute(deviceId, page, pageSize, contentType);
    assertThat(result.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE)).isEqualTo(httpStatus);
    validateListSuccessScenario();
  }

  @Test
  @Order(2)
  void given_MissingDeviceId_When_ListRouteCalled_Then_BadRequest() throws Exception {
    setupListSuccessScenario();
    captureError.expectedMessageCount(1);
    var result = callTestRoute(null, null, null, APPLICATION_JSON);
    captureError.assertIsSatisfied();
    assertThat(result.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE)).isEqualTo(Status.BAD_REQUEST.getStatusCode());
  }

  private static Stream<Arguments> listSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.LIST_READINGS_SUCCESS, "device-001", "0", "50", APPLICATION_JSON, Status.OK.getStatusCode())
    );
  }

  private Exchange callTestRoute(String deviceId, String page, String pageSize, String contentType) throws Exception {
    Map<String, Object> headers = new HashMap<>(defaultHeaders);
    headers.put("deviceId", deviceId);
    headers.put("page", page);
    headers.put("pageSize", pageSize);
    headers.put(HttpHeaders.ACCEPT, contentType);
    exchange.getIn().setHeaders(headers);
    exchange.getIn().setBody(null);
    return fluentProducerTemplate
        .to("direct:testRestListReadings")
        .withExchange(exchange)
        .send();
  }
}
