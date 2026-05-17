package io.fintech.loan.application.service.bbtest.kafka;

import static io.fintech.loan.application.service.utils.testdata.BaseDomainTestDataProducer.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.fintech.loan.application.service.bbtest.UpdateOrderBlackBoxTest;
import io.fintech.loan.application.service.utils.clients.MessageClient;
import io.fintech.loan.application.service.utils.testdata.UpdateOrderDomainTestDataProducer.RequestScenarios;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Black-box test for KAFKA interface UpdateOrder operation.
 * Mirrors KafkaInterfaceUpdateOrderIntegrationTest but uses native Kafka client instead of Camel test routes.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KafkaInterfaceUpdateOrderBlackBoxTest extends UpdateOrderBlackBoxTest {

  static final String UPDATEORDER_BASE_PATH_API = BASE_PATH_API + "%s/updateorder/";
  static final String KAFKA_NORTHBOUND_TOPIC = "camelbeeservice-northbound-updateorder-topic-";

  private final MessageClient messageClient = new MessageClient();

  private static Stream<Arguments> updateOrderSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.UPDATE_ORDER_SUCCESS_ID_FORMAT.formatted(4), "avro", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("updateOrderSuccessParameters")
  void given_ValidOrder_When_UpdateOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(
      String fileName, String payloadFormat, String transactionId) throws Exception {

    setupUpdateOrderSuccessScenario(fileName);

    sendMessage(fileName, payloadFormat, transactionId);

    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
      validateUpdateOrderSuccessScenario(fileName);
    });
  }

  private static Stream<Arguments> updateOrderBadRequestFromTheInterfaceParameters() {
    return Stream.of(
        arguments(RequestScenarios.UPDATE_ORDER_ERROR_NO_ITEMS, "avro", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("updateOrderBadRequestFromTheInterfaceParameters")
  void given_ValidOrder_When_UpdateOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(
      String fileName, String payloadFormat, String transactionId) throws Exception {

    setupUpdateOrderErrorScenario();

    sendMessage(fileName, payloadFormat, transactionId);

    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
      validateUpdateOrderErrorScenario();
    });
  }

  private static Stream<Arguments> updateOrderErrorFromTheRestBackendParameters() {
    return Stream.of(
        arguments(RequestScenarios.UPDATE_ORDER_ERROR_REST_400, "avro", TXID_FOR_REST_400_ERROR),
        arguments(RequestScenarios.UPDATE_ORDER_ERROR_REST_404, "avro", TXID_FOR_REST_404_ERROR),
        arguments(RequestScenarios.UPDATE_ORDER_ERROR_REST_500, "avro", TXID_FOR_REST_500_ERROR)
    );
  }

  @ParameterizedTest
  @Order(3)
  @MethodSource("updateOrderErrorFromTheRestBackendParameters")
  void given_ValidOrder_When_UpdateOrderRouteCalled_And_RestBackendReturnedError_Then_ResultIsError(
      String fileName, String payloadFormat, String transactionId) throws Exception {

    setupUpdateOrderErrorScenario();

    sendMessage(fileName, payloadFormat, transactionId);

    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
      validateUpdateOrderErrorScenario();
    });
  }

  // --- Helper methods ---

  private void sendMessage(String fileName, String payloadFormat, String transactionId) throws Exception {
    String requestFile = UPDATEORDER_BASE_PATH_API.formatted(payloadFormat) + fileName + "." + getFilePostFix(payloadFormat);
    String topicName = KAFKA_NORTHBOUND_TOPIC + payloadFormat;

    if ("proto".equals(payloadFormat) || "avro".equals(payloadFormat)) {
      messageClient.sendKafkaBinaryMessage(topicName, null, readResourceBinary(requestFile), transactionId);
    } else {
      messageClient.sendKafkaMessage(topicName, null, readResource(requestFile), transactionId);
    }
  }

  private static String getFilePostFix(String payloadFormat) {
    return switch (payloadFormat) {
      case "proto" -> "pb";
      case "avro" -> "avro";
      case "json" -> "json";
      case "xml" -> "xml";
      default -> payloadFormat;
    };
  }
}
