package io.fintech.loan.application.service.bbtest.rest;

import static io.fintech.loan.application.service.constants.Constants.APPLICATION_JSON;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.core.type.TypeReference;
import io.fintech.loan.application.service.bbtest.ListOrdersBlackBoxTest;
import io.fintech.loan.application.service.utils.JsonSerDe;
import io.fintech.loan.application.service.utils.testdata.ListOrdersDomainTestDataProducer.RequestScenarios;
import io.restassured.response.Response;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Black-box test for REST interface ListOrders operation.
 * Tests the fully dockerized application from the outside using HTTP calls.
 * Mirrors RestInterfaceListOrdersIntegrationTest but uses RestAssured instead of Camel test routes.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestInterfaceListOrdersBlackBoxTest extends ListOrdersBlackBoxTest {

  static final String ORDER_BASE_URL = "/camelbee-service/orders";

  private static Stream<Arguments> listOrdersSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_1, "ONLINE", "1", "5", 5, "json", APPLICATION_JSON, 200, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_2, "ONLINE", "2", "5", 5, "json", APPLICATION_JSON, 200, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_3, "ONLINE", "3", "5", 2, "json", APPLICATION_JSON, 200, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_4, "ONLINE", "4", "5", 0, "json", APPLICATION_JSON, 200, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_EMPTY, "WHOLESALE", "1", "5", 0, "json", APPLICATION_JSON, 200, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("listOrdersSuccessParameters")
  void given_ValidQueryParameters_When_ListOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(
      String fileName, String salesChannel, String page, String pageSize, int expectedOrders, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupListOrdersSuccessScenario();

    Response response = callTestRoute(salesChannel, page, pageSize, contentType, transactionId);

    assertThat(response.statusCode()).isEqualTo(httpStatus);

    switch (payloadFormat) {
      case "json" -> validateJsonOrders(response, expectedOrders);
      default -> throw new IllegalArgumentException("Unsupported payload format: " + payloadFormat);
    }

    // Allow async backends to complete
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      validateListOrdersSuccessScenario(expectedOrders);
    });

    // Extract nextCursor from response header for cursor-based pagination (NoSQL backends)
    String nextCursor = response.header("nextCursor");
    lastNextCursor = (nextCursor != null) ? nextCursor : "";
  }

  private static Stream<Arguments> listOrdersBadHeaderFromTheInterfaceParameters() {
    return Stream.of(
        arguments("listorders-emptysaleschannel-error-400-request", "", "1", "5", 0, "json", APPLICATION_JSON, 400, TXID_FOR_SUCCESS),
        arguments("listorders-invalidsaleschannel-error-400-request", "AONLINE", "1", "5", 0, "json", APPLICATION_JSON, 400, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("listOrdersBadHeaderFromTheInterfaceParameters")
  void given_InValidQueryParameters_When_ListOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(
      String fileName, String salesChannel, String page, String pageSize, int expectedOrders, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupListOrdersErrorScenario();

    Response response = callTestRoute(salesChannel, page, pageSize, contentType, transactionId);

    assertThat(response.statusCode()).isEqualTo(httpStatus);

    validateListOrdersBadRequestScenario();
  }

  // --- Helper methods (black-box equivalent of callTestRoute in integration test) ---

  private Response callTestRoute(String salesChannel, String page, String pageSize, String contentType, String transactionId) {
    String url = ORDER_BASE_URL + "?salesChannel=" + salesChannel + "&page=" + page + "&pageSize=" + pageSize;
    if (lastNextCursor != null && !lastNextCursor.isEmpty()) {
      url += "&cursor=" + lastNextCursor;
    }
    return given()
        .accept(contentType)
        .header("transactionId", transactionId)
        .when()
        .get(url);
  }

  // --- Response validation methods ---

  private void validateJsonOrders(Response response, int expectedOrders) throws Exception {
    if (expectedOrders == 0) {
      return;
    }
    JsonSerDe<List<io.fintech.loan.application.service.model.api.json.Order>> jsonSerDe = new JsonSerDe<>(
        new TypeReference<List<io.fintech.loan.application.service.model.api.json.Order>>() {
        });

    List<io.fintech.loan.application.service.model.api.json.Order> orders = jsonSerDe.deserialize(response.body().asString());

    assertThat(orders).hasSize(expectedOrders);
  }

}
