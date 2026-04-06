package com.mycompany.catalog.mcp.bbtest.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.catalog.mcp.bbtest.ListProductsBlackBoxTest;
import com.mycompany.catalog.mcp.utils.clients.McpTestClient;
import java.time.Duration;
import java.util.Map;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Black-box test for MCP ListProducts operation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MCP ListProducts Black-Box Tests")
public class McpInterfaceListProductsBlackBoxTest extends ListProductsBlackBoxTest {

  @Test
  @Order(1)
  @DisplayName("Should list products successfully via MCP")
  void test_ListProducts_MCP_Success() {
    setupListProductsSuccessScenario();

    McpTestClient mcpClient = new McpTestClient();
    try {
      mcpClient.connect();
      var result = mcpClient.callTool("listProducts", Map.of(
          "page", 1,
          "pageSize", 10,
          "transactionId", TXID_FOR_SUCCESS
      ));

      assertThat(result).isNotNull();
      assertThat(result.content()).isNotEmpty();

      Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateListProductsSuccessScenario();
      });
    } finally {
      mcpClient.close();
    }
  }

  @Test
  @Order(2)
  @DisplayName("Should return error for invalid page via MCP")
  void test_ListProducts_MCP_InvalidPage() {
    McpTestClient mcpClient = new McpTestClient();
    try {
      mcpClient.connect();
      var result = mcpClient.callTool("listProducts", Map.of(
          "page", 0,
          "pageSize", 10,
          "transactionId", TXID_FOR_SUCCESS
      ));

      assertThat(result).isNotNull();
      assertThat(result.isError()).isTrue();
    } finally {
      mcpClient.close();
    }
  }

}
