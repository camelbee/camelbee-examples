package io.iot.sensor.ingestion.itest.mqtt;

import static io.iot.sensor.ingestion.utils.testdata.SensorReadingTestDataProducer.RequestScenarios;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.iot.sensor.ingestion.itest.IngestReadingIntegrationTest;
import io.iot.sensor.ingestion.itest.IntegrationTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import java.time.Duration;
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

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class MqttInterfaceIngestReadingIntegrationTest extends IngestReadingIntegrationTest {

  Exchange exchange;

  @BeforeAll
  public void setup() throws Exception {
    var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);
    var routeDefinition = new RouteDefinition();
    routeDefinition.from("direct:testMqttIngest").toD(
        "paho-mqtt5:sensors/device-001/readings"
            + "?brokerUrl={{camelbeeservice.mqtt.broker-url}}"
            + "&clientId=test-mqtt-ingest-producer");
    modelContext.addRouteDefinition(routeDefinition);
    super.setup();
  }

  @BeforeEach
  public void setupEndpoints() throws Exception {
    exchange = ExchangeBuilder.anExchange(camelContext).build();
  }

  private static Stream<Arguments> ingestSuccessParameters() {
    return Stream.of(arguments(RequestScenarios.INGEST_READING_SUCCESS));
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("ingestSuccessParameters")
  void given_ValidReading_When_IngestedViaMqtt_Then_ResultIsSuccess(String fileName) throws Exception {
    setupIngestSuccessScenario();
    callTestRoute();
    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
      validateIngestSuccessScenario();
    });
  }

  private Exchange callTestRoute() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put(CONTENT_TYPE, "json");
    exchange.getIn().setHeaders(headers);
    String json = "{\"deviceId\":\"device-001\",\"sensorType\":\"TEMPERATURE\",\"value\":23.5,\"unit\":\"C\",\"quality\":\"GOOD\",\"recordedAt\":\"2025-01-01T00:00:00Z\"}";
    exchange.getIn().setBody(json);
    return fluentProducerTemplate
        .to("direct:testMqttIngest")
        .withExchange(exchange)
        .send();
  }
}
