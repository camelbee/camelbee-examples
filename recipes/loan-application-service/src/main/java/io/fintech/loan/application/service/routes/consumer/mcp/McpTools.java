package io.fintech.loan.application.service.routes.consumer.mcp;

import io.fintech.loan.application.service.model.api.mcp.Order;
import java.util.List;
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

  // =========================================================================
  // GET ORDER
  // =========================================================================

  @McpTool(name = "getOrder", description = "Retrieve a specific order by its ID and sales channel")
  Order getOrder(
      @McpToolParam(description = "The order ID") String id,
      @McpToolParam(description = "Sales channel (e.g. ONLINE, MOBILE, STORE)") String salesChannel,
      @McpToolParam(description = "Client-generated correlation ID for distributed tracing and logging", required = false) String transactionId,
      @McpToolParam(description = "Business process correlation ID for end-to-end transaction tracing across systems",
          required = false) String businessTransactionId
  ) throws Exception {
    log.debug("MCP Tool: getOrder(id: {}, salesChannel: {})", id, salesChannel);

    var result = fluentProducerTemplate
        .to("direct:mcpGetOrder")
        .withHeader("id", id)
        .withHeader("salesChannel", salesChannel)
        .withHeader("transactionId", transactionId)
        .send();

    if (result.getMessage().getBody() instanceof Exception) {
      throw result.getMessage().getBody(Exception.class);
    } else {
      return result.getMessage().getBody(Order.class);
    }
  }

  // =========================================================================
  // LIST ORDERS
  // =========================================================================

  @McpTool(name = "listOrders", description = "List orders with pagination, filtered by sales channel")
  List<Order> listOrders(
      @McpToolParam(description = "Sales channel to filter by") String salesChannel,
      @McpToolParam(description = "Page number", required = false) int page,
      @McpToolParam(description = "Number of orders per page", required = false) int pageSize,
      @McpToolParam(description = "Cursor for cursor-based pagination (returned as nextCursor from previous page)", required = false) String cursor,
      @McpToolParam(description = "Client-generated correlation ID for distributed tracing and logging", required = false) String transactionId,
      @McpToolParam(description = "Business process correlation ID for end-to-end transaction tracing across systems",
          required = false) String businessTransactionId
  ) throws Exception {
    log.debug("MCP Tool: listOrders(salesChannel: {}, page: {}, pageSize: {})", salesChannel, page, pageSize);

    var result = fluentProducerTemplate
        .to("direct:mcpListOrders")
        .withHeader("salesChannel", salesChannel)
        .withHeader("page", page)
        .withHeader("pageSize", pageSize)
        .withHeader("cursor", cursor)
        .withHeader("transactionId", transactionId)
        .send();

    if (result.getMessage().getBody() instanceof Exception) {
      throw result.getMessage().getBody(Exception.class);
    } else {
      return result.getMessage().getBody(List.class);
    }
  }

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
    log.debug("MCP Tool: createOrder(salesChannel: {}, items: {}, transactionId: {}, businessTransactionId: {})",
        order.getSalesChannel(), order.getItems(), transactionId, businessTransactionId);

    var result = fluentProducerTemplate
        .to("direct:mcpCreateOrder")
        .withHeader("transactionId", transactionId)
        .withBody(order)
        .send();

    if (result.getMessage().getBody() instanceof Exception) {
      throw result.getMessage().getBody(Exception.class);
    } else {
      return result.getMessage().getBody(Order.class);
    }
  }

}
