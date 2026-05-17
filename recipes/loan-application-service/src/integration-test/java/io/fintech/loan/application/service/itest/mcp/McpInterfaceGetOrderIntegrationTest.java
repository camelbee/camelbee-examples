package io.fintech.loan.application.service.itest.mcp;

import static io.fintech.loan.application.service.utils.testdata.BaseDomainTestDataProducer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.core.type.TypeReference;
import io.fintech.loan.application.service.itest.GetOrderIntegrationTest;
import io.fintech.loan.application.service.utils.JsonSerDe;
import io.fintech.loan.application.service.utils.testdata.GetOrderDomainTestDataProducer.RequestScenarios;
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
 * Integration test for MCP interface GetOrder operation.
 */
class McpInterfaceGetOrderIntegrationTest extends GetOrderIntegrationTest {

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

  private static Stream<Arguments> getOrderSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.GET_ORDER_SUCCESS, "ONLINE", "1", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("getOrderSuccessParameters")
  void given_ValidOrderId_When_ListOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(
      String fileName, String salesChannel, String orderId, String transactionId) throws Exception {

    setupGetOrderSuccessScenario();

    CallToolResult result = client.callTool(new CallToolRequest("getOrder", buildToolArgs(orderId, salesChannel, transactionId)));
    assertThat(result).isNotNull();
    assertThat(result.content()).isNotEmpty();
    validateMcpOrderFromText(((McpSchema.TextContent) result.content().getFirst()).text());

    validateGetOrderSuccessScenario(fileName, orderId, salesChannel);
  }

  // =========================================================================
  // NOT FOUND
  // =========================================================================

  private static Stream<Arguments> getOrderNotFoundParameters() {
    return Stream.of(
        arguments(RequestScenarios.GET_ORDER_SUCCESS_NOTFOUND_COMBINATION_1, "ONLINE", "111", TXID_FOR_SUCCESS),
        arguments(RequestScenarios.GET_ORDER_SUCCESS_NOTFOUND_COMBINATION_2, "WHOLESALE", "1", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("getOrderNotFoundParameters")
  void given_InValidOrderIdOrSalesChanel_When_GetOrderRouteCalled_Then_ResultIsNotFound(
      String fileName, String salesChannel, String orderId, String transactionId) throws Exception {

    setupGetOrderNotFoundScenario();

    CallToolResult result = client.callTool(new CallToolRequest("getOrder", buildToolArgs(orderId, salesChannel, transactionId)));
    assertThat(result).isNotNull();
    assertThat(result.isError()).isTrue();

    validateGetOrderNotFoundScenario(orderId, salesChannel);
  }

  // =========================================================================
  // BAD REQUEST FROM INTERFACE
  // =========================================================================

  private static Stream<Arguments> getOrderBadPathOrQueryFromTheInterfaceParameters() {
    return Stream.of(
        arguments("getorder-invalidorderid-error-404-request", "ONLINE", "nonnumeric", TXID_FOR_SUCCESS),
        arguments("getorder-emptysaleschannel-error-400-request", "", "1", TXID_FOR_SUCCESS),
        arguments("getorder-invalidsaleschannel-error-400-request", "AONLINE", "1", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(3)
  @MethodSource("getOrderBadPathOrQueryFromTheInterfaceParameters")
  void given_InValidPathOrHeaderParameters_When_GetOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(
      String fileName, String salesChannel, String orderId, String transactionId) throws Exception {

    setupGetOrderBadRequestFromTheInterfaceScenario();

    CallToolResult result = client.callTool(new CallToolRequest("getOrder", buildToolArgs(orderId, salesChannel, transactionId)));
    assertThat(result).isNotNull();
    assertThat(result.isError()).isTrue();

    validateBadRequestFromTheInterfaceScenario();
  }

  // =========================================================================
  // HELPERS
  // =========================================================================

  private Map<String, Object> buildToolArgs(String orderId, String salesChannel, String transactionId) {
    return Map.of(
        "arg0", orderId,
        "arg1", salesChannel,
        "arg2", transactionId
    );
  }

  private void validateMcpOrderFromText(String resultText) {
    try {
      JsonSerDe<io.fintech.loan.application.service.model.api.mcp.Order> jsonOrderSerDe = new JsonSerDe<>(new TypeReference<>() {
      });
      io.fintech.loan.application.service.model.api.mcp.Order order = jsonOrderSerDe.deserialize(resultText);

      List<String> itemIds = order.getItems().stream()
          .map(io.fintech.loan.application.service.model.api.mcp.OrderItem::getId).toList();

      assertThat(order.getOrderDate()).isNotNull();
      assertThat(order.getLastUpdateTimestamp()).isNotNull();
      assertThat(itemIds).hasSize(5);
      assertThat(itemIds).allMatch(Objects::nonNull);
    } catch (Exception e) {
      throw new RuntimeException("Failed to validate MCP order response", e);
    }
  }

}
