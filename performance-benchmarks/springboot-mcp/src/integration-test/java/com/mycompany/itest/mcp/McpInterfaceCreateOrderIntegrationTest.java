package com.mycompany.itest.mcp;

import static com.mycompany.utils.testdata.BaseDomainTestDataProducer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mycompany.itest.CreateOrderIntegrationTest;
import com.mycompany.utils.JsonSerDe;
import com.mycompany.utils.testdata.CreateOrderDomainTestDataProducer.RequestScenarios;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Integration test for MCP interface CreateOrder operation.
 */
class McpInterfaceCreateOrderIntegrationTest extends CreateOrderIntegrationTest {

  private static final int INTERFACE_RETRY_COUNT = 1;
  private static final JsonSerDe<Map<String, Object>> REQUEST_SERDE = new JsonSerDe<>(new TypeReference<>() {
  });

  @LocalServerPort
  int port;

  McpSyncClient client;

  @BeforeAll
  public void setup() throws Exception {
    super.setup();
  }

  @BeforeEach
  public void setupEndpoints() throws Exception {

    HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
        .builder("http://localhost:" + port + "/mcp")
        .build();
    client = McpClient.sync(transport).build();
    client.initialize();

  }

  // =========================================================================
  // SUCCESS
  // =========================================================================

  private static Stream<Arguments> createOrderSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.CREATE_ORDER_SUCCESS, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("createOrderSuccessParameters")
  void given_ValidOrder_When_CreateOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(
      String fileName, String transactionId) throws Exception {

    setupCreateOrderSuccessScenario(fileName);

    CallToolResult result = client.callTool(new CallToolRequest("createOrder", buildToolArgs(fileName, transactionId)));
    assertThat(result).isNotNull();
    assertThat(result.content()).isNotEmpty();
    validateMcpOrderFromText(((McpSchema.TextContent) result.content().getFirst()).text());

    validateCreateOrderSuccessScenario(fileName);
  }

  // =========================================================================
  // BAD REQUEST FROM INTERFACE
  // =========================================================================

  private static Stream<Arguments> createOrderBadRequestFromTheInterfaceParameters() {
    return Stream.of(
        arguments(RequestScenarios.CREATE_ORDER_ERROR_NO_ITEMS, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("createOrderBadRequestFromTheInterfaceParameters")
  void given_InValidOrder_When_CreateOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(
      String fileName, String transactionId) throws Exception {

    setupCreateOrderBadRequestFromTheInterfaceScenario(fileName, INTERFACE_RETRY_COUNT, true);

    CallToolResult result = client.callTool(new CallToolRequest("createOrder", buildToolArgs(fileName, transactionId)));
    assertThat(result).isNotNull();
    assertThat(result.isError()).isTrue();

    validateBadRequestFromTheInterfaceScenario();
  }

  // =========================================================================
  // HELPERS
  // =========================================================================

  private Map<String, Object> buildToolArgs(String fileName, String transactionId) throws Exception {
    String requestFile = CREATEORDER_BASE_PATH_API.formatted("mcp") + fileName + ".json";
    Map<String, Object> order = REQUEST_SERDE.deserialize(readResource(requestFile));
    return Map.of("arg0", order,
        "arg1", transactionId
    );
  }

  private void validateMcpOrderFromText(String resultText) {
    try {
      JsonSerDe<com.mycompany.model.api.mcp.Order> jsonOrderSerDe = new JsonSerDe<>(new TypeReference<>() {
      });

      com.mycompany.model.api.mcp.Order order = jsonOrderSerDe.deserialize(resultText);

      List<String> itemIds = order.getItems().stream()
          .map(com.mycompany.model.api.mcp.OrderItem::getId).toList();

      assertThat(order.getId()).isNotEmpty();
      assertThat(order.getOrderDate()).isNotNull();
      assertThat(order.getLastUpdateTimestamp()).isNotNull();
      assertThat(itemIds).hasSize(10);
      assertThat(itemIds).allMatch(Objects::nonNull);
    } catch (Exception e) {
      throw new RuntimeException("Failed to validate MCP order response", e);
    }
  }

}
