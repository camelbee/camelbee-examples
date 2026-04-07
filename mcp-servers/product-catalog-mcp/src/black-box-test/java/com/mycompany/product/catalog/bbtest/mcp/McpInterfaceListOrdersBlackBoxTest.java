package com.mycompany.product.catalog.bbtest.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.mycompany.product.catalog.bbtest.ListOrdersBlackBoxTest;
import com.mycompany.product.catalog.utils.clients.McpTestClient;
import com.mycompany.product.catalog.utils.testdata.ListOrdersDomainTestDataProducer.RequestScenarios;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Black-box test for MCP interface ListOrders operation.
 * Tests the fully dockerized application from the outside using MCP protocol.
 * Mirrors McpInterfaceListOrdersIntegrationTest but uses McpTestClient instead of Camel test routes.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class McpInterfaceListOrdersBlackBoxTest extends ListOrdersBlackBoxTest {

  // =========================================================================
  // SUCCESS
  // =========================================================================

  private static Stream<Arguments> listOrdersSuccessParameters() {
    return Stream.of(
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_1, "ONLINE", "1", "5", 5, TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("listOrdersSuccessParameters")
  void given_ValidQueryParameters_When_McpListOrdersCalled_Then_ResultIsSuccess(
      String fileName, String salesChannel, String page, String pageSize, int expectedOrders, String transactionId) throws Exception {
    setupListOrdersSuccessScenario();

    var mcpClient = new McpTestClient();
    try {
      java.util.Map<String, Object> args = new java.util.HashMap<>();
      args.put("salesChannel", salesChannel);
      args.put("page", Integer.parseInt(page));
      args.put("pageSize", Integer.parseInt(pageSize));
      args.put("cursor", "");
      args.put("transactionId", transactionId);
      var result = mcpClient.callTool("listOrders", args);

      assertThat(result).isNotNull();
      assertThat(result.content()).isNotEmpty();

      await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateListOrdersSuccessScenario(0);
      });
    } finally {
      mcpClient.close();
    }
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
  void given_InValidQueryParameters_When_McpListOrdersCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(
      String fileName, String salesChannel, String transactionId) throws Exception {
    setupListOrdersErrorScenario();

    var mcpClient = new McpTestClient();
    try {
      java.util.Map<String, Object> args = new java.util.HashMap<>();
      args.put("salesChannel", salesChannel);
      args.put("page", 1);
      args.put("pageSize", 5);
      args.put("cursor", "");
      args.put("transactionId", transactionId);
      var result = mcpClient.callTool("listOrders", args);

      await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateListOrdersBadRequestScenario();
      });
    } finally {
      mcpClient.close();
    }
  }

  // =========================================================================
  // REST BACKEND ERROR
  // =========================================================================

  private static Stream<Arguments> listOrdersErrorFromTheRestBackendParameters() {
    return Stream.of(
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_1, "ONLINE", "1", "5", 5, TXID_FOR_REST_400_ERROR),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_2, "ONLINE", "2", "5", 5, TXID_FOR_REST_404_ERROR),
        arguments(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_3, "ONLINE", "3", "5", 5, TXID_FOR_REST_500_ERROR)
    );
  }

  @ParameterizedTest
  @Order(3)
  @MethodSource("listOrdersErrorFromTheRestBackendParameters")
  void given_ValidQueryParameters_When_McpListOrdersCalled_And_RestBackendReturnedError_Then_ResultIsError(
      String fileName, String salesChannel, String page, String pageSize, int expectedOrders, String transactionId) throws Exception {
    setupListOrdersErrorScenario();

    var mcpClient = new McpTestClient();
    try {
      java.util.Map<String, Object> args = new java.util.HashMap<>();
      args.put("salesChannel", salesChannel);
      args.put("page", Integer.parseInt(page));
      args.put("pageSize", Integer.parseInt(pageSize));
      args.put("cursor", "");
      args.put("transactionId", transactionId);
      var result = mcpClient.callTool("listOrders", args);

      await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateErrorFromTheRestBackendScenario(fileName);
      });
    } finally {
      mcpClient.close();
    }
  }

}
