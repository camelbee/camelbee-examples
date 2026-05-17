package io.fintech.loan.application.service.itest.mcp;

import static io.fintech.loan.application.service.utils.testdata.BaseDomainTestDataProducer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.core.type.TypeReference;
import io.fintech.loan.application.service.itest.ListOrdersIntegrationTest;
import io.fintech.loan.application.service.utils.JsonSerDe;
import io.fintech.loan.application.service.utils.testdata.ListOrdersDomainTestDataProducer.RequestScenarios;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Integration test for MCP interface ListOrders operation.
 */
class McpInterfaceListOrdersIntegrationTest extends ListOrdersIntegrationTest {

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

  private static Stream<Arguments> listOrdersSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_1, "ONLINE", "1", "5", 5, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_2, "ONLINE", "2", "5", 5, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_3, "ONLINE", "3", "5", 2, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_4, "ONLINE", "4", "5", 0, TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_EMPTY, "WHOLESALE", "1", "5", 0, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("listOrdersSuccessParameters")
  void given_ValidQueryParameters_When_ListOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess(
      String fileName, String salesChannel, String page, String pageSize, int expectedOrders, String transactionId) throws Exception {

    setupListOrdersSuccessScenario();

    final boolean validateSize = true;

    CallToolResult result = client.callTool(new CallToolRequest("listOrders", buildToolArgs(salesChannel, page, pageSize, transactionId)));
    assertThat(result).isNotNull();

    if (validateSize) {
      validateMcpOrdersFromText(((McpSchema.TextContent) result.content().getFirst()).text(), expectedOrders);
    }

    validateListOrdersSuccessScenario(fileName, page, pageSize, salesChannel, expectedOrders);
  }

  // =========================================================================
  // BAD REQUEST FROM INTERFACE
  // =========================================================================

  private static Stream<Arguments> listOrdersBadHeaderFromTheInterfaceParameters() {
    return Stream.of(
        arguments(RequestScenarios.LIST_ORDERS_ERROR_EMPTY_SALES_CHANNEL, "", TXID_FOR_SUCCESS),
        arguments(RequestScenarios.LIST_ORDERS_ERROR_INVALID_SALES_CHANNEL, "INVALID", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("listOrdersBadHeaderFromTheInterfaceParameters")
  void given_InValidQueryParameters_When_ListOrderRouteCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(
      String fileName, String salesChannel, String transactionId) throws Exception {

    setupListOrdersBadRequestFromTheInterfaceScenario();

    CallToolResult result = client.callTool(new CallToolRequest("listOrders", buildToolArgs(salesChannel, "1", "5", transactionId)));
    assertThat(result).isNotNull();
    assertThat(result.isError()).isTrue();

    validateBadRequestFromTheInterfaceScenario();
  }

  // =========================================================================
  // HELPERS
  // =========================================================================

  private Map<String, Object> buildToolArgs(String salesChannel, String page, String pageSize, String transactionId) {
    return Map.of(
        "arg0", salesChannel,
        "arg1", Integer.parseInt(page),
        "arg2", Integer.parseInt(pageSize),
        "arg3", lastNextCursor != null ? lastNextCursor : "",
        "arg4", transactionId
    );
  }

  private void validateMcpOrdersFromText(String resultText, int expectedOrder) {
    try {
      JsonSerDe<List<io.fintech.loan.application.service.model.api.mcp.Order>> jsonOrdersSerDe = new JsonSerDe<>(new TypeReference<>() {
      });

      List<io.fintech.loan.application.service.model.api.mcp.Order> orders = jsonOrdersSerDe.deserialize(resultText);

      assertThat(orders).hasSize(expectedOrder);
    } catch (Exception e) {
      throw new RuntimeException("Failed to validate MCP orders response", e);
    }
  }

}
