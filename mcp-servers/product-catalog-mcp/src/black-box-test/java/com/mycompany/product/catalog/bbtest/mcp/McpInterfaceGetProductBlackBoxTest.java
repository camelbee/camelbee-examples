package com.mycompany.product.catalog.bbtest.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.mycompany.product.catalog.bbtest.GetProductBlackBoxTest;
import com.mycompany.product.catalog.utils.clients.McpTestClient;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MCP GetProduct Black-Box Tests")
public class McpInterfaceGetProductBlackBoxTest extends GetProductBlackBoxTest {

  private static Stream<Arguments> getProductSuccessParameters() {
    return Stream.of(
        arguments("prod-001", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("getProductSuccessParameters")
  @DisplayName("Should get product by ID successfully via MCP")
  void given_ValidId_When_McpGetProductCalled_Then_ResultIsSuccess(
      String productId, String transactionId) throws Exception {
    setupGetProductSuccessScenario();

    var mcpClient = new McpTestClient();
    try {
      java.util.Map<String, Object> args = new java.util.HashMap<>();
      args.put("id", productId);
      args.put("userId", "test-user");
      args.put("transactionId", transactionId);
      var result = mcpClient.callTool("getProduct", args);

      assertThat(result).isNotNull();
      assertThat(result.content()).isNotEmpty();

      await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateGetProductSuccessScenario(productId);
      });
    } finally {
      mcpClient.close();
    }
  }

  private static Stream<Arguments> getProductErrorParameters() {
    return Stream.of(
        arguments("", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("getProductErrorParameters")
  @DisplayName("Should return error when product ID is empty via MCP")
  void given_EmptyId_When_McpGetProductCalled_Then_ResultIsError(
      String productId, String transactionId) throws Exception {
    setupGetProductErrorScenario();

    var mcpClient = new McpTestClient();
    try {
      java.util.Map<String, Object> args = new java.util.HashMap<>();
      args.put("id", productId);
      args.put("userId", "test-user");
      args.put("transactionId", transactionId);
      var result = mcpClient.callTool("getProduct", args);

      await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateGetProductBadRequestScenario();
      });
    } finally {
      mcpClient.close();
    }
  }
}
