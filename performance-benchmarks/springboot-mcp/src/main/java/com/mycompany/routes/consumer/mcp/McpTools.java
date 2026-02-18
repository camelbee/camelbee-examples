package com.mycompany.routes.consumer.mcp;

import com.mycompany.mapper.api.McpOrderMapper;
import com.mycompany.model.api.mcp.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.FluentProducerTemplate;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

/**
 * MCP (Model Context Protocol) Tools for handling order management operations.
 *
 * <p>This class exposes MCP tools for order operations including:
 * - Order creation (single and batch)
 * - Order retrieval and listing
 * - Order updates and replacements
 * - Order deletion
 *
 * @author camelbee
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class McpTools {

  private final FluentProducerTemplate fluentProducerTemplate;
  private final McpOrderMapper mcpOrderMapper;
  // =========================================================================
  // CREATE ORDER
  // =========================================================================

  @McpTool(name = "createOrder", description = "Create a new order with customer details, product information, and shipping preferences")
  Order createOrder(
      @McpToolParam(description = "Order object containing salesChannel, items with productName, quantity, and price") Order order,
      @McpToolParam(description = "Client-generated correlation ID for distributed tracing and logging", required = false) String transactionId,
      @McpToolParam(description = "Business process correlation ID for end-to-end transaction tracing across systems",
          required = false) String businessTransactionId
  ) throws Exception {

    // =============================================================================
    // TEST 1 — Baseline: Pure MCP Tool (no mapping, no Camel)
    // -----------------------------------------------------------------------------
    // Goal: Measure raw MCP server overhead on Quarkus JVM, Quarkus Native,
    //       and Spring Boot. No business logic, no framework involvement.
    //       Just receive the MCP tool call and return a plain Order object.
    //
    // Expected: Fastest response times across all 3 platforms.
    //           Quarkus Native should shine here with lowest memory + cold start.
    // =============================================================================
    //    Order response = new Order();
    //    response.setId("1");
    //    response.setSalesChannel(order.getSalesChannel());
    //    response.setItems(order.getItems());
    //    return response;

    // =============================================================================
    // TEST 2 — MCP Tool + MapStruct (object mapping layer added)
    // -----------------------------------------------------------------------------
    // Goal: Introduce a realistic DTO → Domain → DTO conversion via MapStruct.
    //       Simulates the mapping overhead that exists in real production services.
    //
    //       Flow: MCP Order (API model)
    //               ↓  mcpToDomainOrder()
    //             Domain Order (internal model)
    //               ↓  domainToMcpOrder()
    //             MCP Order (API model) → returned to MCP client
    //
    // Expected: Minimal overhead vs Test 1 since MapStruct generates plain Java code
    //           at compile time (no reflection). Native image impact should be low,
    //           but watch for GraalVM pruning MapStruct-generated classes.
    // =============================================================================
    com.mycompany.model.domain.Order domainOrder = mcpOrderMapper.mcpToDomainOrder(order);
    return mcpOrderMapper.domainToMcpOrder(domainOrder);

    // =============================================================================
    // TEST 3 — MCP Tool + MapStruct + Apache Camel (full production stack)
    // -----------------------------------------------------------------------------
    // Goal: Measure the full enterprise integration stack. The MCP tool delegates
    //       to a Camel route via FluentProducerTemplate. The Camel route handles
    //       the mapping (MapStruct) and any downstream integration logic.
    //
    //       Flow: MCP Tool call
    //               ↓  FluentProducerTemplate
    //             direct:mcpCreateOrder  (Camel route entry point)
    //               ↓  MapStruct mapping inside the route
    //             Domain logic / downstream systems
    //               ↓  mapped back to MCP Order
    //             MCP Tool response → returned to MCP client
    //
    // Expected: Highest overhead due to Camel route initialization and message
    //           exchange wrapping. However, this reflects real-world CamelBee
    //           generated microservice performance — the most meaningful benchmark.
    //           Quarkus Native should still outperform JVM on cold start + memory.
    // =============================================================================

    //  var result = fluentProducerTemplate
    //      .to("direct:mcpCreateOrder")
    //      .withHeader("transactionId", transactionId)
    //      .withBody(order)
    //      .send();
    //
    //  if (result.getMessage().getBody() instanceof Exception) {
    //    throw result.getMessage().getBody(Exception.class);
    //  } else {
    //    return result.getMessage().getBody(Order.class);
    //  }
  }

}
