package com.mycompany.catalog.mcp.bbtest.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.catalog.mcp.bbtest.GetProductBlackBoxTest;
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
 * Black-box test for MCP GetProduct operation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MCP GetProduct Black-Box Tests")
public class McpInterfaceGetProductBlackBoxTest extends GetProductBlackBoxTest {

  @Test
  @Order(1)
  @DisplayName("Should get product successfully via MCP")
  void test_GetProduct_MCP_Success() {
    setupGetProductSuccessScenario();

    McpTestClient mcpClient = new McpTestClient();
    try {
      mcpClient.connect();
      var result = mcpClient.callTool("getProduct", Map.of(
          "productId", "prod-001",
          "transactionId", TXID_FOR_SUCCESS
      ));

      assertThat(result).isNotNull();
      assertThat(result.content()).isNotEmpty();

      Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateGetProductSuccessScenario();
      });
    } finally {
      mcpClient.close();
    }
  }

  @Test
  @Order(2)
  @DisplayName("Should return error for empty product ID via MCP")
  void test_GetProduct_MCP_EmptyId() {
    McpTestClient mcpClient = new McpTestClient();
    try {
      mcpClient.connect();
      var result = mcpClient.callTool("getProduct", Map.of(
          "productId", "",
          "transactionId", TXID_FOR_SUCCESS
      ));

      assertThat(result).isNotNull();
      assertThat(result.isError()).isTrue();
    } finally {
      mcpClient.close();
    }
  }

}
