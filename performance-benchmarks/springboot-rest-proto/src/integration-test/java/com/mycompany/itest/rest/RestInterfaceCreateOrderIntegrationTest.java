package com.mycompany.itest.rest;

import static com.mycompany.constants.Constants.APPLICATION_PROTOBUF;
import static com.mycompany.utils.testdata.BaseDomainTestDataProducer.*;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.mycompany.itest.CreateOrderIntegrationTest;
import com.mycompany.utils.ProtobufSerDe;
import com.mycompany.utils.testdata.CreateOrderDomainTestDataProducer.RequestScenarios;
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
 * Integration test for REST interface CreateOrder operation.
 */

class RestInterfaceCreateOrderIntegrationTest extends CreateOrderIntegrationTest {

  static final String ORDER_BASE_URL = "/camelbee-service/orders";

  Exchange exchange;

  Map<String, Object> defaultHeaders;

  /*
   * This REST service implements a single-attempt policy (no retries).
   * If the endpoint call fails, the error is immediately returned to the client.
   */
  private final int interfaceRetryCount = 1;

  @BeforeAll
  public void setup() throws Exception {

    var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);

    var routeDefinition = new RouteDefinition();
    routeDefinition.from("direct:testRestCreateOrder").to(
        "http:localhost:{{local.server.port}}" + ORDER_BASE_URL + "?throwExceptionOnFailure=false");

    modelContext.addRouteDefinition(routeDefinition);

    defaultHeaders = Map.of(
        HttpHeaders.ACCEPT, "*/*",
        "CamelHttpMethod", "POST",
        "CamelHttpCharacterEncoding", "UTF-8");

    super.setup();
  }

  @BeforeEach
  public void setupEndpoints() throws Exception {
    exchange = ExchangeBuilder.anExchange(camelContext).build();

  }

  private static Stream<Arguments> createOrderSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.CREATE_ORDER_SUCCESS, "proto", APPLICATION_PROTOBUF, HttpStatus.CREATED.value(), TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("createOrderSuccessParameters")
  void given_ValidOrder_When_CreateOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(String fileName, String payloadFormat, String contentType,
      int httpStatus, String transactionId) throws Exception {

    setupCreateOrderSuccessScenario(fileName);

    var result = callTestRoute(fileName, payloadFormat, contentType, transactionId);

    assertThat(result.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE)).isEqualTo(httpStatus);

    switch (payloadFormat) {
      case "proto" -> validateProtoOrder(result);
      default -> throw new IllegalArgumentException("Unsupported file postfix: " + payloadFormat);
    }

    validateCreateOrderSuccessScenario(fileName);

  }

  private static Stream<Arguments> createOrderBadRequestFromTheInterfaceParameters() {
    return Stream.of(
        arguments(RequestScenarios.CREATE_ORDER_ERROR_NO_ITEMS, "proto", APPLICATION_PROTOBUF, HttpStatus.BAD_REQUEST.value(), TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("createOrderBadRequestFromTheInterfaceParameters")
  void given_InValidOrder_When_CreateOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(String fileName, String payloadFormat,
      String contentType, int httpStatus, String transactionId) throws Exception {

    setupCreateOrderBadRequestFromTheInterfaceScenario(fileName, interfaceRetryCount, true);

    var result = callTestRoute(fileName, payloadFormat, contentType, transactionId);

    assertThat(result.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE)).isEqualTo(httpStatus);

    validateBadRequestFromTheInterfaceScenario();
  }

  private Exchange callTestRoute(String fileName, String payloadFormat, String contentType, String transactionId) throws Exception {
    Map<String, Object> headers = new HashMap<>(defaultHeaders);
    headers.put(CONTENT_TYPE, contentType);
    headers.put(HttpHeaders.ACCEPT, contentType);
    headers.put("transactionId", transactionId);

    exchange.getIn().setHeaders(headers);

    String requestFile = CREATEORDER_BASE_PATH_API.formatted(payloadFormat) + fileName + "." + getFilePostFix(payloadFormat);

    setBody(exchange, payloadFormat, requestFile);

    var result = fluentProducerTemplate
        .to("direct:testRestCreateOrder")
        .withExchange(exchange)
        .send();

    return result;
  }

  private void validateProtoOrder(Exchange result) throws Exception {
    ProtobufSerDe<com.mycompany.model.api.proto.Order> protoSerDe = new ProtobufSerDe<>(
        com.mycompany.model.api.proto.Order.parser());

    com.mycompany.model.api.proto.Order response = protoSerDe.deserialize(result.getMessage().getBody(byte[].class));

    validateOrderCommonAssertions(
        response.getId(),
        response.getOrderDate(),
        response.getLastUpdateTimestamp(),
        response.getItemsList().stream().map(com.mycompany.model.api.proto.OrderItem::getId).toList()
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
