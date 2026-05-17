package io.fintech.loan.application.service.itest.kafka;

import static io.fintech.loan.application.service.utils.testdata.BaseDomainTestDataProducer.*;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.fintech.loan.application.service.itest.UpdateOrderIntegrationTest;
import io.fintech.loan.application.service.utils.testdata.UpdateOrderDomainTestDataProducer.RequestScenarios;
import java.time.Duration;
import java.util.Arrays;
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

/**
 * Integration test for Kafka interface UpdateOrder operation.
 */
class KafkaInterfaceUpdateOrderIntegrationTest extends UpdateOrderIntegrationTest {

  Exchange exchange;

  /*
   * Unlike JMS, Kafka does not provide a built-in retry mechanism for message processing failures.
   * In JMS, failed messages can be redelivered automatically based on configuration.
   * With Kafka, retries must be handled manually using techniques like manual offset commits, retry topics, or dead-letter queues.
   * In this case, we are proceeding without a retry mechanism.
   */
  private final int interfaceRetryCount = 1;

  @BeforeAll
  public void setup() throws Exception {

    var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);

    var routeDefinitionBinary = new RouteDefinition();
    routeDefinitionBinary.from("direct:testKafkaUpdateOrderBinary").toD(
        "kafka:{{camelbeeservice.northbound-updateorder-topic}}-${header.Content-Type}?" +
            "valueSerializer=org.apache.kafka.common.serialization.ByteArraySerializer" +
            "&keySerializer=org.apache.kafka.common.serialization.ByteArraySerializer");

    var routeDefinitionDefault = new RouteDefinition();
    routeDefinitionDefault.from("direct:testKafkaUpdateOrderDefault").toD(
        "kafka:{{camelbeeservice.northbound-updateorder-topic}}-${header.Content-Type}");

    modelContext.addRouteDefinitions(Arrays.asList(routeDefinitionBinary, routeDefinitionDefault));

    var routeDefinitionAvroSR = new RouteDefinition();
    routeDefinitionAvroSR.from("direct:testKafkaUpdateOrderAvroSR").to(
        "kafka:{{camelbeeservice.northbound-updateorder-topic}}-avro"
            + "?valueSerializer=io.apicurio.registry.serde.avro.AvroKafkaSerializer"
            + "&keySerializer=org.apache.kafka.common.serialization.StringSerializer");
    modelContext.addRouteDefinition(routeDefinitionAvroSR);

    super.setup();
  }

  @BeforeEach
  public void setupEndpoints() throws Exception {
    exchange = ExchangeBuilder.anExchange(camelContext).build();

    wireMock.resetRequests();

  }

  private static Stream<Arguments> updateOrderSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.UPDATE_ORDER_SUCCESS_ID_FORMAT.formatted(4), "4", "avro", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("updateOrderSuccessParameters")
  void given_ValidOrder_When_UpdateOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(String fileName, String orderId,
      String payloadFormat, String transactionId) throws Exception {

    setupUpdateOrderSuccessScenario(fileName);

    callTestRoute(fileName, payloadFormat, transactionId);

    // wait till the route is processed.
    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {

      validateUpdateOrderSuccessScenario(fileName, orderId);

    });
  }

  private static Stream<Arguments> updateOrderBadRequestFromTheInterfaceParameters() {
    return Stream.of(
        arguments(RequestScenarios.UPDATE_ORDER_ERROR_NO_ITEMS, "5", "avro", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("updateOrderBadRequestFromTheInterfaceParameters")
  void given_ValidOrder_When_UpdateOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(String fileName, String orderId,
      String payloadFormat, String transactionId) throws Exception {

    setupUpdateOrderBadRequestFromTheInterfaceScenario(fileName, interfaceRetryCount, true);

    callTestRoute(fileName, payloadFormat, transactionId);

    // wait till the route is processed.
    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {

      validateBadRequestFromTheInterfaceScenario(fileName, orderId);

    });

  }

  private static Stream<Arguments> updateOrderErrorFromTheRestBackendParameters() {
    return Stream.of(
        arguments(RequestScenarios.UPDATE_ORDER_ERROR_REST_400, "7", "avro", TXID_FOR_REST_400_ERROR),
        arguments(RequestScenarios.UPDATE_ORDER_ERROR_REST_404, "7", "avro", TXID_FOR_REST_404_ERROR),
        arguments(RequestScenarios.UPDATE_ORDER_ERROR_REST_500, "7", "avro", TXID_FOR_REST_500_ERROR)
    );
  }

  @ParameterizedTest
  @Order(3)
  @MethodSource("updateOrderErrorFromTheRestBackendParameters")
  void given_ValidOrder_When_UpdateOrderRouteCalled_And_RestBackendReturnedError_Then_ResultIsError(String fileName, String orderId,
      String payloadFormat, String transactionId) throws Exception {

    setupUpdateOrderErrorFromTheRestBackendScenario(interfaceRetryCount, true);

    callTestRoute(fileName, payloadFormat, transactionId);

    // wait till the route is processed.
    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {

      validateErrorFromTheRestBackendScenario(fileName, orderId, interfaceRetryCount);

    });

  }

  private Exchange callTestRoute(String fileName, String payloadFormat, String transactionId) throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put(CONTENT_TYPE, payloadFormat);
    headers.put("transactionId", transactionId);

    exchange.getIn().setHeaders(headers);

    String requestFile = UPDATEORDER_BASE_PATH_API.formatted(payloadFormat) + fileName + "." + getFilePostFix(payloadFormat);

    String endpoint;
    // SR via Apicurio covers Avro only in this archetype. JSON, XML and Proto
    // use the plain (non-SR) Camel paths.
    if ("avro".equalsIgnoreCase(payloadFormat)) {
      setBodyForKafkaSchemaRegistry(exchange, payloadFormat, requestFile, io.fintech.loan.application.service.model.api.avro.Order.class);
      endpoint = "direct:testKafkaUpdateOrderAvroSR";
    } else {
      setBody(exchange, payloadFormat, requestFile);
      endpoint = isBinaryFormat(payloadFormat) ? "direct:testKafkaUpdateOrderBinary" : "direct:testKafkaUpdateOrderDefault";
    }

    return fluentProducerTemplate
        .to(endpoint)
        .withExchange(exchange)
        .send();

  }
}
