package com.mycompany.product.catalog.bbtest.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.mycompany.product.catalog.bbtest.ListProductsBlackBoxTest;
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
@DisplayName("MCP ListProducts Black-Box Tests")
public class McpInterfaceListProductsBlackBoxTest extends ListProductsBlackBoxTest {

  private static Stream<Arguments> listProductsSuccessParameters() {
    return Stream.of(
        arguments("1", "5", TXID_FOR_SUCCESS)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("listProductsSuccessParameters")
  @DisplayName("Should list products successfully via MCP")
  void given_ValidParams_When_McpListProductsCalled_Then_ResultIsSuccess(
      String page, String pageSize, String transactionId) throws Exception {
    setupListProductsSuccessScenario();

    var mcpClient = new McpTestClient();
    try {
      java.util.Map<String, Object> args = new java.util.HashMap<>();
      args.put("page", Integer.parseInt(page));
      args.put("pageSize", Integer.parseInt(pageSize));
      args.put("userId", "test-user");
      args.put("transactionId", transactionId);
      var result = mcpClient.callTool("listProducts", args);

      assertThat(result).isNotNull();
      assertThat(result.content()).isNotEmpty();

      await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateListProductsSuccessScenario();
      });
    } finally {
      mcpClient.close();
    }
  }

  private static Stream<Arguments> listProductsErrorParameters() {
    return Stream.of(
        arguments("1", "5", TXID_FOR_REST_500_ERROR)
    );
  }

  @ParameterizedTest
  @Order(2)
  @MethodSource("listProductsErrorParameters")
  @DisplayName("Should return error when REST backend fails via MCP")
  void given_ValidParams_When_McpListProductsCalled_And_RestBackendFails_Then_ResultIsError(
      String page, String pageSize, String transactionId) throws Exception {
    setupListProductsErrorScenario();

    var mcpClient = new McpTestClient();
    try {
      java.util.Map<String, Object> args = new java.util.HashMap<>();
      args.put("page", Integer.parseInt(page));
      args.put("pageSize", Integer.parseInt(pageSize));
      args.put("userId", "test-user");
      args.put("transactionId", transactionId);
      var result = mcpClient.callTool("listProducts", args);

      await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateListProductsRestBackendErrorScenario();
      });
    } finally {
      mcpClient.close();
    }
  }
}
