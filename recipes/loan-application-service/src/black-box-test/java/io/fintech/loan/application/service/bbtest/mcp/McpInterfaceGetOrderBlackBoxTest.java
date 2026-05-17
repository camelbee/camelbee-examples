package io.fintech.loan.application.service.bbtest.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.fintech.loan.application.service.bbtest.GetOrderBlackBoxTest;
import io.fintech.loan.application.service.utils.clients.McpTestClient;
import io.fintech.loan.application.service.utils.testdata.GetOrderDomainTestDataProducer.RequestScenarios;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Black-box test for MCP interface GetOrder operation.
 * Tests the fully dockerized application from the outside using MCP protocol.
 * Mirrors McpInterfaceGetOrderIntegrationTest but uses McpTestClient instead of Camel test routes.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class McpInterfaceGetOrderBlackBoxTest extends GetOrderBlackBoxTest {

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
  void given_ValidOrderId_When_McpGetOrderCalled_Then_ResultIsSuccess(
      String fileName, String salesChannel, String orderId, String transactionId) throws Exception {
    setupGetOrderSuccessScenario();

    var mcpClient = new McpTestClient();
    try {
      java.util.Map<String, Object> args = new java.util.HashMap<>();
      args.put("arg0", orderId);
      args.put("arg1", salesChannel);
      args.put("arg2", transactionId);
      var result = mcpClient.callTool("getOrder", args);

      assertThat(result).isNotNull();
      assertThat(result.content()).isNotEmpty();

      await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateGetOrderSuccessScenario();
      });
    } finally {
      mcpClient.close();
    }
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
  @Order(2)
  @MethodSource("getOrderBadPathOrQueryFromTheInterfaceParameters")
  void given_InValidPathOrHeaderParameters_When_McpGetOrderCalled_And_InterfaceReturnedBadRequest_Then_ResultIsBadRequest(
      String fileName, String salesChannel, String orderId, String transactionId) throws Exception {
    setupGetOrderErrorScenario();

    var mcpClient = new McpTestClient();
    try {
      java.util.Map<String, Object> args = new java.util.HashMap<>();
      args.put("arg0", orderId);
      args.put("arg1", salesChannel);
      args.put("arg2", transactionId);
      var result = mcpClient.callTool("getOrder", args);

      await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
        validateGetOrderBadRequestScenario();
      });
    } finally {
      mcpClient.close();
    }
  }

}
