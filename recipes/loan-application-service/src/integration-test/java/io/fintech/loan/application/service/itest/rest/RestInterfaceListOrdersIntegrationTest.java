package io.fintech.loan.application.service.itest.rest;

import static io.fintech.loan.application.service.constants.Constants.APPLICATION_JSON;
import static io.fintech.loan.application.service.utils.testdata.BaseDomainTestDataProducer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.core.type.TypeReference;
import io.fintech.loan.application.service.itest.ListOrdersIntegrationTest;
import io.fintech.loan.application.service.utils.JsonSerDe;
import io.fintech.loan.application.service.utils.testdata.ListOrdersDomainTestDataProducer.RequestScenarios;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * Integration test for REST interface ListOrders operation.
 */

class RestInterfaceListOrdersIntegrationTest extends ListOrdersIntegrationTest {

  static final String ORDER_BASE_URL = "/camelbee-service/orders";

  Exchange exchange;

  Map<String, Object> defaultHeaders;

  @BeforeAll
  public void setup() throws Exception {

    var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);

    var routeDefinition = new RouteDefinition();
    routeDefinition.from("direct:testRestListOrder").to(
        "http:localhost:{{local.server.port}}" + ORDER_BASE_URL + "?throwExceptionOnFailure=false");

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

  private static Stream<Arguments> listOrdersSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_1, "ONLINE", "1", "5", 5, "json", APPLICATION_JSON, HttpStatus.OK.value(), TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_2, "ONLINE", "2", "5", 5, "json", APPLICATION_JSON, HttpStatus.OK.value(), TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_3, "ONLINE", "3", "5", 2, "json", APPLICATION_JSON, HttpStatus.OK.value(), TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_4, "ONLINE", "4", "5", 0, "json", APPLICATION_JSON, HttpStatus.OK.value(), TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_EMPTY, "WHOLESALE", "1", "5", 0, "json", APPLICATION_JSON, HttpStatus.OK.value(), TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("listOrdersSuccessParameters")
  void given_ValidQueryParameters_When_ListOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(
      String fileName, String salesChannel, String page, String pageSize, int expectedOrders, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupListOrdersSuccessScenario();

    var result = callTestRoute(salesChannel, page, pageSize, contentType, transactionId);

    assertThat(result.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE)).isEqualTo(httpStatus);

    switch (payloadFormat) {
      case "json" -> validateJsonOrder(result, expectedOrders);
      default -> throw new IllegalArgumentException("Unsupported file postfix: " + payloadFormat);
    }

    validateListOrdersSuccessScenario(fileName, page, pageSize, salesChannel, expectedOrders);

    // Extract nextCursor response header and chain it for next call
    String nextCursor = result.getMessage().getHeader("nextCursor", String.class);
    lastNextCursor = (nextCursor != null) ? nextCursor : "";
  }

  private static Stream<Arguments> listOrdersBadHeaderFromTheInterfaceParameters() {
    return Stream.of(
        arguments("listorders-emptypage-error-400-request", "ONLINE", "", "5", 5, "json", APPLICATION_JSON, HttpStatus.BAD_REQUEST.value(), TXID_FOR_SUCCESS),
        arguments("listorders-emptypagesize-error-400-request", "ONLINE", "1", "", 5, "json", APPLICATION_JSON, HttpStatus.BAD_REQUEST.value(),
            TXID_FOR_SUCCESS),
        arguments("listorders-emptysaleschannel-error-400-request", "", "3", "5", 2, "json", APPLICATION_JSON, HttpStatus.BAD_REQUEST.value(),
            TXID_FOR_SUCCESS),
        arguments("listorders-invalidpage-error-400-request", "ONLINE", "a1", "5", 5, "json", APPLICATION_JSON, HttpStatus.BAD_REQUEST.value(),
            TXID_FOR_SUCCESS),
        arguments("listorders-invalidpagesize-error-400-request", "ONLINE", "1", "a5", 5, "json", APPLICATION_JSON, HttpStatus.BAD_REQUEST.value(),
            TXID_FOR_SUCCESS),
        arguments("listorders-invalidsaleschannel-error-400-request", "AONLINE", "3", "5", 2, "json", APPLICATION_JSON, HttpStatus.BAD_REQUEST.value(),
            TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("listOrdersBadHeaderFromTheInterfaceParameters")
  void given_InValidQueryParameters_When_ListOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(
      String fileName, String salesChannel, String page, String pageSize, int expectedOrders, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupListOrdersBadRequestFromTheInterfaceScenario();

    var result = callTestRoute(salesChannel, page, pageSize, contentType, transactionId);

    assertThat(result.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE)).isEqualTo(httpStatus);

    validateBadRequestFromTheInterfaceScenario();
  }

  private Exchange callTestRoute(String salesChannel, String page, String pageSize, String contentType, String transactionId) throws Exception {
    Map<String, Object> headers = new HashMap<>(defaultHeaders);
    headers.put("salesChannel", salesChannel);
    headers.put("page", page);
    headers.put("pageSize", pageSize);
    headers.put(HttpHeaders.ACCEPT, contentType);
    headers.put("transactionId", transactionId);
    headers.put("cursor", lastNextCursor);

    exchange.getIn().setHeaders(headers);
    exchange.getIn().setBody(null);

    return fluentProducerTemplate
        .to("direct:testRestListOrder")
        .withExchange(exchange)
        .send();
  }

  private void validateJsonOrder(Exchange result, int expectedOrder) throws Exception {
    JsonSerDe<List<io.fintech.loan.application.service.model.api.json.Order>> jsonSerDe = new JsonSerDe<>(
        new TypeReference<List<io.fintech.loan.application.service.model.api.json.Order>>() {
        });

    List<io.fintech.loan.application.service.model.api.json.Order> response = jsonSerDe.deserialize(result.getMessage().getBody(String.class));

    assertThat(response).hasSize(expectedOrder);
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
