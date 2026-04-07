package com.mycompany.product.catalog.routes.consumer.mcp;

import com.mycompany.product.catalog.model.api.mcp.Order;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import io.smallrye.common.annotation.Blocking;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.FluentProducerTemplate;

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
@Singleton
@Blocking  // FluentProducerTemplate.send() is blocking; must not run on the vert.x event loop
@Slf4j
public class McpTools {

  @Inject
  FluentProducerTemplate fluentProducerTemplate;

  @Inject
  RoutingContext routingContext;

  // =========================================================================
  // LIST ORDERS
  // =========================================================================

  @Tool(description = "List orders with pagination, filtered by sales channel")
  List<Order> listOrders(
      @ToolArg(description = "Sales channel to filter by") String salesChannel,
      @ToolArg(description = "Page number", defaultValue = "1") int page,
      @ToolArg(description = "Number of orders per page", defaultValue = "10") int pageSize,
      @ToolArg(description = "Cursor for cursor-based pagination (returned as nextCursor from previous page)", required = false) String cursor,
      @ToolArg(description = "Client-generated correlation ID for distributed tracing and logging", required = false) String transactionId,
      @ToolArg(description = "Business process correlation ID for end-to-end transaction tracing across systems", required = false) String businessTransactionId
  ) {
    log.debug("MCP Tool: listOrders(salesChannel: {}, page: {}, pageSize: {})", salesChannel, page, pageSize);

    var result = fluentProducerTemplate
        .to("direct:mcpListOrders")
        .withHeader("salesChannel", salesChannel)
        .withHeader("page", page)
        .withHeader("pageSize", pageSize)
        .withHeader("cursor", cursor)
        .withHeader("transactionId", transactionId)
        .send();

    if (result.getMessage().getBody() instanceof ToolCallException) {
      throw result.getMessage().getBody(ToolCallException.class);
    } else {
      return result.getMessage().getBody(List.class);
    }

  }

  // =========================================================================
  // CREATE ORDER
  // =========================================================================

  @Tool(description = "Create a new order with customer details, product information, and shipping preferences")
  Order createOrder(
      @ToolArg(description = "Order object containing salesChannel, items with productName, quantity, and price") Order order,
      @ToolArg(description = "Client-generated correlation ID for distributed tracing and logging", required = false) String transactionId,
      @ToolArg(description = "Business process correlation ID for end-to-end transaction tracing across systems", required = false) String businessTransactionId
  ) {
    log.debug("MCP Tool: createOrder(salesChannel: {}, items: {}, transactionId: {}, businessTransactionId: {})",
        order.getSalesChannel(), order.getItems(), transactionId, businessTransactionId);

    var result = fluentProducerTemplate
        .to("direct:mcpCreateOrder")
        .withHeader("transactionId", transactionId)
        .withBody(order)
        .send();

    if (result.getMessage().getBody() instanceof ToolCallException) {
      throw result.getMessage().getBody(ToolCallException.class);
    } else {
      return result.getMessage().getBody(Order.class);
    }
  }

}