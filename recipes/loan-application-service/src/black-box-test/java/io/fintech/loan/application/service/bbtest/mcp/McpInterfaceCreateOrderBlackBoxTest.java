package io.fintech.loan.application.service.bbtest.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.fintech.loan.application.service.bbtest.CreateOrderBlackBoxTest;
import io.fintech.loan.application.service.utils.clients.McpTestClient;
import io.fintech.loan.application.service.utils.testdata.CreateOrderDomainTestDataProducer.RequestScenarios;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Black-box test for MCP interface CreateOrder operation.
 * Tests the fully dockerized application from the outside using MCP protocol.
 * Mirrors McpInterfaceCreateOrderIntegrationTest but uses McpTestClient instead of Camel test routes.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class McpInterfaceCreateOrderBlackBoxTest extends CreateOrderBlackBoxTest {

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
  void given_ValidOrder_When_McpCreateOrderCalled_Then_ResultIsSuccess(String fileName, String transactionId) throws Exception {
    setupCreateOrderSuccessScenario(fileName);

    var mcpClient = new McpTestClient();
    try {
      var result = mcpClient.callToolWithOrder("createOrder",
          "/data/inttest/api/mcp/createorder/" + fileName + ".json",
          transactionId);

      assertThat(result).isNotNull();
      assertThat(result.content()).isNotEmpty();

      await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateCreateOrderSuccessScenario(fileName);
      });
    } finally {
      mcpClient.close();
    }
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
  void given_InValidOrder_When_McpCreateOrderCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(String fileName,
      String transactionId) throws Exception {
    setupCreateOrderErrorScenario();

    var mcpClient = new McpTestClient();
    try {
      var result = mcpClient.callToolWithOrder("createOrder",
          "/data/inttest/api/mcp/createorder/" + fileName + ".json",
          transactionId);

      // MCP returns isError for bad requests
      await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateCreateOrderBadRequestScenario();
      });
    } finally {
      mcpClient.close();
    }
  }

}
