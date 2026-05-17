package io.fintech.loan.application.service.bbtest.rest;

import static io.fintech.loan.application.service.constants.Constants.APPLICATION_JSON;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.core.type.TypeReference;
import io.fintech.loan.application.service.bbtest.CreateOrderBlackBoxTest;
import io.fintech.loan.application.service.utils.JsonSerDe;
import io.fintech.loan.application.service.utils.testdata.CreateOrderDomainTestDataProducer.RequestScenarios;
import io.restassured.response.Response;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Black-box test for REST interface CreateOrder operation.
 * Tests the fully dockerized application from the outside using HTTP calls.
 * Mirrors RestInterfaceCreateOrderIntegrationTest but uses RestAssured instead of Camel test routes.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestInterfaceCreateOrderBlackBoxTest extends CreateOrderBlackBoxTest {

  static final String ORDER_BASE_URL = "/camelbee-service/orders";

  private static Stream<Arguments> createOrderSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.CREATE_ORDER_SUCCESS, "json", APPLICATION_JSON, 201, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("createOrderSuccessParameters")
  void given_ValidOrder_When_CreateOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(String fileName, String payloadFormat, String contentType,
      int httpStatus, String transactionId) throws Exception {

    setupCreateOrderSuccessScenario(fileName);

    Response response = callTestRoute(fileName, payloadFormat, contentType, transactionId);

    assertThat(response.statusCode()).isEqualTo(httpStatus);

    switch (payloadFormat) {
      case "json" -> validateJsonOrder(response);
      default -> throw new IllegalArgumentException("Unsupported payload format: " + payloadFormat);
    }

    // Allow async backends to complete
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      validateCreateOrderSuccessScenario(fileName);
    });
  }

  private static Stream<Arguments> createOrderBadRequestFromTheInterfaceParameters() {
    return Stream.of(
        arguments("createorder-interface-invaliddate-error-400-request", "json", APPLICATION_JSON, 400, TXID_FOR_SUCCESS),
        arguments("createorder-interface-invalidjsonbody-error-400-request", "json", APPLICATION_JSON, 400, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.CREATE_ORDER_ERROR_NO_ITEMS, "json", APPLICATION_JSON, 400, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("createOrderBadRequestFromTheInterfaceParameters")
  void given_InValidOrder_When_CreateOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(String fileName, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupCreateOrderErrorScenario();

    Response response = callTestRoute(fileName, payloadFormat, contentType, transactionId);

    assertThat(response.statusCode()).isEqualTo(httpStatus);

    validateCreateOrderBadRequestScenario();
  }

  // --- Helper methods (black-box equivalent of callTestRoute in integration test) ---

  private Response callTestRoute(String fileName, String payloadFormat, String contentType, String transactionId) {
    String requestFile = CREATEORDER_BASE_PATH_API.formatted(payloadFormat) + fileName + "." + getFilePostFix(payloadFormat);

    var reqSpec = given()
        .contentType(contentType)
        .accept(contentType)
        .header("transactionId", transactionId);

    if ("proto".equals(payloadFormat) || "avro".equals(payloadFormat)) {
      reqSpec.body(readResourceBinary(requestFile));
    } else {
      reqSpec.body(readResource(requestFile));
    }

    return reqSpec.when().post(ORDER_BASE_URL);
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

  // --- Response validation methods (black-box equivalent of integration test validation) ---

  private void validateJsonOrder(Response response) throws Exception {
    JsonSerDe<io.fintech.loan.application.service.model.api.json.Order> jsonSerDe = new JsonSerDe<>(
        new TypeReference<io.fintech.loan.application.service.model.api.json.Order>() {
        });

    io.fintech.loan.application.service.model.api.json.Order order = jsonSerDe.deserialize(response.body().asString());

    validateOrderCommonAssertions(
        order.getId(),
        order.getOrderDate(),
        order.getLastUpdateTimestamp(),
        order.getItems().stream().map(io.fintech.loan.application.service.model.api.json.OrderItem::getId).toList()
    );
  }

  private void validateOrderCommonAssertions(
      String orderId,
      Object orderDate,
      Object lastUpdateTimestamp,
      List<String> itemIds) {

    assertThat(orderId).isNotEmpty();
    assertThat(orderDate).isNotNull();
    assertThat(lastUpdateTimestamp).isNotNull();
    assertThat(itemIds).hasSize(10);
    assertThat(itemIds).allMatch(Objects::nonNull);
  }

}
