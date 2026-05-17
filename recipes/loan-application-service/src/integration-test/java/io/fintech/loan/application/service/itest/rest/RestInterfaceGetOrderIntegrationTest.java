package io.fintech.loan.application.service.itest.rest;

import static io.fintech.loan.application.service.constants.Constants.APPLICATION_JSON;
import static io.fintech.loan.application.service.utils.testdata.BaseDomainTestDataProducer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.core.type.TypeReference;
import io.fintech.loan.application.service.itest.GetOrderIntegrationTest;
import io.fintech.loan.application.service.utils.JsonSerDe;
import io.fintech.loan.application.service.utils.testdata.GetOrderDomainTestDataProducer.RequestScenarios;
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
 * Integration test for REST interface GetOrder operation.
 */

class RestInterfaceGetOrderIntegrationTest extends GetOrderIntegrationTest {

  static final String ORDER_BASE_URL = "/camelbee-service/orders";

  Exchange exchange;

  Map<String, Object> defaultHeaders;

  @BeforeAll
  public void setup() throws Exception {

    var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);

    var routeDefinition = new RouteDefinition();
    routeDefinition.from("direct:testRestGetOrder").to(
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

  private static Stream<Arguments> getOrderSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.GET_ORDER_SUCCESS, "ONLINE", "1", "json", APPLICATION_JSON, HttpStatus.OK.value(), TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("getOrderSuccessParameters")
  void given_ValidOrderId_When_ListOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(
      String fileName, String salesChannel, String orderId, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupGetOrderSuccessScenario();

    var result = callTestRoute(salesChannel, orderId, contentType, transactionId);

    assertThat(result.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE)).isEqualTo(httpStatus);

    switch (payloadFormat) {
      case "json" -> validateJsonOrder(result);
      default -> throw new IllegalArgumentException("Unsupported file postfix: " + payloadFormat);
    }

    validateGetOrderSuccessScenario(fileName, orderId, salesChannel);
  }

  private static Stream<Arguments> getOrderNotFoundParameters() {
    return Stream.of(
        arguments(RequestScenarios.GET_ORDER_SUCCESS, "ONLINE", "111", "json", APPLICATION_JSON, HttpStatus.NOT_FOUND.value(), TXID_FOR_SUCCESS),
        arguments(RequestScenarios.GET_ORDER_SUCCESS, "WHOLESALE", "1", "json", APPLICATION_JSON, HttpStatus.NOT_FOUND.value(), TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("getOrderNotFoundParameters")
  void given_InValidOrderIdOrSalesChanel_When_GetOrderRouteCalled_Then_ResultIsNotFound(
      String fileName, String salesChannel, String orderId, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupGetOrderNotFoundScenario();

    var result = callTestRoute(salesChannel, orderId, contentType, transactionId);

    assertThat(result.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE)).isEqualTo(httpStatus);

    validateGetOrderNotFoundScenario(orderId, salesChannel);
  }

  private static Stream<Arguments> getOrderBadPathOrQueryFromTheInterfaceParameters() {
    return Stream.of(
        arguments("getorder-invalidorderid-error-404-request", "ONLINE", "nonnumeric", "json", APPLICATION_JSON, HttpStatus.BAD_REQUEST.value(),
            TXID_FOR_SUCCESS),
        arguments("getorder-emptysaleschannel-error-400-request", "", "1", "json", APPLICATION_JSON, HttpStatus.BAD_REQUEST.value(), TXID_FOR_SUCCESS),
        arguments("getorder-invalidsaleschannel-error-400-request", "AONLINE", "1", "json", APPLICATION_JSON, HttpStatus.BAD_REQUEST.value(), TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(3)
  @MethodSource("getOrderBadPathOrQueryFromTheInterfaceParameters")
  void given_InValidPathOrHeaderParameters_When_GetOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(
      String fileName, String salesChannel, String orderId, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupGetOrderBadRequestFromTheInterfaceScenario();

    var result = callTestRoute(salesChannel, orderId, contentType, transactionId);

    assertThat(result.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE)).isEqualTo(httpStatus);

    validateBadRequestFromTheInterfaceScenario();
  }

  private static Stream<Arguments> getOrderEmptyPathParameterFromTheInterfaceParameters() {
    return Stream.of(
        arguments("getorder-emptyorderId-error-404-request", "ONLINE", " ", "json", APPLICATION_JSON, HttpStatus.BAD_REQUEST.value(), TXID_FOR_SUCCESS)
    );
  }

  /*
  Returns 404 at Camel REST DSL layer, bypassing RestConsumerRoute processing
  */
  @ParameterizedTest
  @Order(4)
  @MethodSource("getOrderEmptyPathParameterFromTheInterfaceParameters")
  void given_EmptyPathParameter_When_GetOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(
      String fileName, String salesChannel, String orderId, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupGetOrderEmptyPathParameterFromTheInterfaceScenario();

    var result = callTestRoute(salesChannel, orderId, contentType, transactionId);

    assertThat(result.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE)).isEqualTo(httpStatus);

    validateEmptyPathParameterFromTheInterfaceScenario();
  }

  private Exchange callTestRoute(String salesChannel, String orderId, String contentType, String transactionId) throws Exception {
    Map<String, Object> headers = new HashMap<>(defaultHeaders);
    headers.put("salesChannel", salesChannel);
    headers.put(Exchange.HTTP_PATH, "/" + orderId);
    headers.put(HttpHeaders.ACCEPT, contentType);
    headers.put("transactionId", transactionId);

    exchange.getIn().setHeaders(headers);
    exchange.getIn().setBody(null);

    return fluentProducerTemplate
        .to("direct:testRestGetOrder")
        .withExchange(exchange)
        .send();
  }

  private void validateJsonOrder(Exchange result) throws Exception {
    JsonSerDe<io.fintech.loan.application.service.model.api.json.Order> jsonSerDe = new JsonSerDe<>(
        new TypeReference<io.fintech.loan.application.service.model.api.json.Order>() {
        });

    io.fintech.loan.application.service.model.api.json.Order response = jsonSerDe.deserialize(result.getMessage().getBody(String.class));

    validateOrderCommonAssertions(
        response.getId(),
        response.getOrderDate(),
        response.getLastUpdateTimestamp(),
        response.getItems().stream().map(io.fintech.loan.application.service.model.api.json.OrderItem::getId).toList()
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
