package com.mycompany.itest.grpc;

import static com.mycompany.utils.testdata.BaseDomainTestDataProducer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.mycompany.itest.CreateOrderIntegrationTest;
import com.mycompany.itest.IntegrationTestProfile;
import com.mycompany.utils.ProtobufSerDe;
import com.mycompany.utils.testdata.CreateOrderDomainTestDataProducer.RequestScenarios;
import io.grpc.StatusRuntimeException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import java.util.List;
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

/**
 * Integration test for SOAP interface CreateOrder operation.
 */

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class GrpcInterfaceCreateOrderIntegrationTest extends CreateOrderIntegrationTest {

  Exchange exchange;

  /*
   * This GRPC service operates with a single attempt policy (no retries).
   * If the endpoint call fails, the error is immediately returned to the client.
   */
  private final int interfaceRetryCount = 1;

  @BeforeAll
  public void setup() throws Exception {

    var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);

    var routeDefinition = new RouteDefinition();
    routeDefinition.from("direct:testGrpcCreateOrder").to("grpc://localhost:{{camelbee.grpc-server.port}}" +
        "/com.mycompany.order.grpc.OrderService" +
        "?method=CreateOrder" +
        "&forwardOnError=true" +
        "&forwardOnCompleted=true" +
        "&inheritExchangePropertiesForReplies=true"
    );

    modelContext.addRouteDefinition(routeDefinition);

    super.setup();

  }

  @BeforeEach
  public void setupEndpoints() throws Exception {
    exchange = ExchangeBuilder.anExchange(camelContext).build();

  }

  private static Stream<Arguments> createOrderSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.CREATE_ORDER_SUCCESS, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("createOrderSuccessParameters")
  void given_ValidOrder_When_CreateOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(String fileName, String transactionId) throws Exception {

    setupCreateOrderSuccessScenario(fileName);

    var result = callTestRoute(fileName, transactionId);

    com.mycompany.order.grpc.CreateOrderResponse createOrderResponse = (com.mycompany.order.grpc.CreateOrderResponse) result.getMessage().getBody(List.class)
        .get(0);

    assertThat(createOrderResponse.getOrder().getId()).isNotEmpty();
    assertThat(createOrderResponse.getOrder().getOrderDate()).isNotNull();
    assertThat(createOrderResponse.getOrder().getLastUpdateTimestamp()).isNotNull();
    assertThat(createOrderResponse.getOrder().getItemsList()).hasSize(10);
    assertThat(createOrderResponse.getOrder().getItemsList()).allMatch(Objects::nonNull);

    validateCreateOrderSuccessScenario(fileName);
  }

  private static Stream<Arguments> createOrderBadRequestFromTheInterfaceParameters() {
    return Stream.of(
        arguments(RequestScenarios.CREATE_ORDER_ERROR_NO_ITEMS, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("createOrderBadRequestFromTheInterfaceParameters")
  void given_ValidOrder_When_CreateOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(String fileName,
      String transactionId) throws Exception {

    setupCreateOrderBadRequestFromTheInterfaceScenario(fileName, interfaceRetryCount, true);

    var result = callTestRoute(fileName, transactionId);

    StatusRuntimeException statusRuntimeException = result.getException(StatusRuntimeException.class);

    assertThat(statusRuntimeException.getMessage()).isNotEmpty();
    assertThat(statusRuntimeException.getMessage()).contains("ERROR-VALIDATION-001");

    validateBadRequestFromTheInterfaceScenario();
  }

  private Exchange callTestRoute(String fileName, String transactionId) throws Exception {

    String requestFile = CREATEORDER_BASE_PATH_API.formatted("grpc") + fileName + ".pb.json";

    String requestJson = readResource(requestFile);

    ProtobufSerDe<com.mycompany.order.grpc.CreateOrderRequest> protobufSerDe = new ProtobufSerDe<>(com.mycompany.order.grpc.CreateOrderRequest.parser());

    // Convert JSON to CreateOrderRequest proto object
    com.mycompany.order.grpc.CreateOrderRequest request = protobufSerDe.deserializeFromJson(requestJson);

    com.mycompany.order.grpc.CreateOrderRequest.Builder builder = com.mycompany.order.grpc.CreateOrderRequest.newBuilder();
    builder.mergeFrom(request);
    builder.setTransactionId(transactionId);

    // Set the proto object in the exchange body
    exchange.getIn().setBody(builder.build());

    var result = fluentProducerTemplate
        .to("direct:testGrpcCreateOrder")
        .withExchange(exchange)
        .send();

    return result;
  }

}
