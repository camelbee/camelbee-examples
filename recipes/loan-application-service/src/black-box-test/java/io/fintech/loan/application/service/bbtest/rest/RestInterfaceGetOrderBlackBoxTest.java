package io.fintech.loan.application.service.bbtest.rest;

import static io.fintech.loan.application.service.constants.Constants.APPLICATION_JSON;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.core.type.TypeReference;
import io.fintech.loan.application.service.bbtest.GetOrderBlackBoxTest;
import io.fintech.loan.application.service.utils.JsonSerDe;
import io.fintech.loan.application.service.utils.testdata.GetOrderDomainTestDataProducer.RequestScenarios;
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
 * Black-box test for REST interface GetOrder operation.
 * Tests the fully dockerized application from the outside using HTTP calls.
 * Mirrors RestInterfaceGetOrderIntegrationTest but uses RestAssured instead of Camel test routes.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestInterfaceGetOrderBlackBoxTest extends GetOrderBlackBoxTest {

  static final String ORDER_BASE_URL = "/camelbee-service/orders";

  private static Stream<Arguments> getOrderSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.GET_ORDER_SUCCESS, "ONLINE", "1", "json", APPLICATION_JSON, 200, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("getOrderSuccessParameters")
  void given_ValidOrderId_When_GetOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(
      String fileName, String salesChannel, String orderId, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupGetOrderSuccessScenario();

    Response response = callTestRoute(salesChannel, orderId, contentType, transactionId);

    assertThat(response.statusCode()).isEqualTo(httpStatus);

    switch (payloadFormat) {
      case "json" -> validateJsonOrder(response);
      default -> throw new IllegalArgumentException("Unsupported payload format: " + payloadFormat);
    }

    // Allow async backends to complete
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      validateGetOrderSuccessScenario();
    });
  }

  private static Stream<Arguments> getOrderNotFoundParameters() {
    return Stream.of(
        arguments(RequestScenarios.GET_ORDER_SUCCESS, "ONLINE", "111", "json", APPLICATION_JSON, 404, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.GET_ORDER_SUCCESS, "WHOLESALE", "1", "json", APPLICATION_JSON, 404, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("getOrderNotFoundParameters")
  void given_InValidOrderIdOrSalesChannel_When_GetOrderRouteCalled_Then_ResultIsNotFound(
      String fileName, String salesChannel, String orderId, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupGetOrderErrorScenario();

    Response response = callTestRoute(salesChannel, orderId, contentType, transactionId);

    assertThat(response.statusCode()).isEqualTo(httpStatus);

    validateGetOrderNotFoundScenario();
  }

  private static Stream<Arguments> getOrderBadPathOrQueryFromTheInterfaceParameters() {
    return Stream.of(
        arguments("getorder-invalidorderid-error-404-request", "ONLINE", "nonnumeric", "json", APPLICATION_JSON, 400, TXID_FOR_SUCCESS),
        arguments("getorder-emptysaleschannel-error-400-request", "", "1", "json", APPLICATION_JSON, 400, TXID_FOR_SUCCESS),
        arguments("getorder-invalidsaleschannel-error-400-request", "AONLINE", "1", "json", APPLICATION_JSON, 400, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(3)
  @MethodSource("getOrderBadPathOrQueryFromTheInterfaceParameters")
  void given_InValidPathOrHeaderParameters_When_GetOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(
      String fileName, String salesChannel, String orderId, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupGetOrderErrorScenario();

    Response response = callTestRoute(salesChannel, orderId, contentType, transactionId);

    assertThat(response.statusCode()).isEqualTo(httpStatus);

    validateGetOrderBadRequestScenario();
  }

  /*
  Skipped in BB tests: Empty path parameter (" ") in a real HTTP URL gets trimmed/resolved
  to the ListOrders endpoint (200), not rejected as 400 like in integration tests where
  the orderId is passed as a Camel header to the REST DSL layer.
  */

  // --- Helper methods (black-box equivalent of callTestRoute in integration test) ---

  private Response callTestRoute(String salesChannel, String orderId, String contentType, String transactionId) {
    return given()
        .accept(contentType)
        .queryParam("salesChannel", salesChannel)
        .header("transactionId", transactionId)
        .when()
        .get(ORDER_BASE_URL + "/" + orderId);
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
    assertThat(itemIds).hasSize(5);
    assertThat(itemIds).allMatch(Objects::nonNull);
  }

}
