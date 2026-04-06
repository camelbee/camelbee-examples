package com.mycompany.catalog.mcp.routes.consumer.mcp;

import com.mycompany.catalog.mcp.model.api.mcp.Product;
import com.mycompany.catalog.mcp.model.api.mcp.ProductPage;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.FluentProducerTemplate;

/**
 * MCP (Model Context Protocol) Tools for product catalog operations.
 *
 * <p>This class exposes MCP tools for:
 * - Paginated product listing
 * - Filtered product search
 * - Single product retrieval
 */
@Singleton
@Blocking
@Slf4j
public class McpTools {

  @Inject
  FluentProducerTemplate fluentProducerTemplate;

  // =========================================================================
  // LIST PRODUCTS
  // =========================================================================

  @Tool(description = "List products with pagination")
  ProductPage listProducts(
      @ToolArg(description = "Page number", defaultValue = "1") int page,
      @ToolArg(description = "Number of products per page", defaultValue = "10") int pageSize,
      @ToolArg(description = "User ID for audit logging", required = false) String userId,
      @ToolArg(description = "Client-generated correlation ID for distributed tracing", required = false) String transactionId
  ) {
    log.debug("MCP Tool: listProducts(page: {}, pageSize: {})", page, pageSize);

    var result = fluentProducerTemplate
        .to("direct:mcpListProducts")
        .withHeader("page", page)
        .withHeader("pageSize", pageSize)
        .withHeader("userId", userId)
        .withHeader("toolName", "listProducts")
        .withHeader("toolParameters", String.format("{\"page\":%d,\"pageSize\":%d}", page, pageSize))
        .withHeader("transactionId", transactionId)
        .send();

    if (result.getMessage().getBody() instanceof ToolCallException) {
      throw result.getMessage().getBody(ToolCallException.class);
    } else {
      return result.getMessage().getBody(ProductPage.class);
    }
  }

  // =========================================================================
  // SEARCH PRODUCTS
  // =========================================================================

  @Tool(description = "Search products with filters: text query, category, price range, and stock availability")
  ProductPage searchProducts(
      @ToolArg(description = "Text search query", required = false) String query,
      @ToolArg(description = "Category filter", required = false) String category,
      @ToolArg(description = "Minimum price filter", required = false) Double minPrice,
      @ToolArg(description = "Maximum price filter", required = false) Double maxPrice,
      @ToolArg(description = "Filter for in-stock products only", required = false) Boolean inStock,
      @ToolArg(description = "Page number", defaultValue = "1") int page,
      @ToolArg(description = "Number of products per page", defaultValue = "10") int pageSize,
      @ToolArg(description = "User ID for audit logging", required = false) String userId,
      @ToolArg(description = "Client-generated correlation ID for distributed tracing", required = false) String transactionId
  ) {
    log.debug("MCP Tool: searchProducts(query: {}, category: {}, page: {}, pageSize: {})", query, category, page, pageSize);

    var result = fluentProducerTemplate
        .to("direct:mcpSearchProducts")
        .withHeader("query", query)
        .withHeader("category", category)
        .withHeader("minPrice", minPrice)
        .withHeader("maxPrice", maxPrice)
        .withHeader("inStock", inStock)
        .withHeader("page", page)
        .withHeader("pageSize", pageSize)
        .withHeader("userId", userId)
        .withHeader("toolName", "searchProducts")
        .withHeader("toolParameters", String.format(
            "{\"query\":\"%s\",\"category\":\"%s\",\"minPrice\":%s,\"maxPrice\":%s,\"inStock\":%s,\"page\":%d,\"pageSize\":%d}",
            query != null ? query : "", category != null ? category : "",
            minPrice != null ? minPrice : "null", maxPrice != null ? maxPrice : "null",
            inStock != null ? inStock : "null", page, pageSize))
        .withHeader("transactionId", transactionId)
        .send();

    if (result.getMessage().getBody() instanceof ToolCallException) {
      throw result.getMessage().getBody(ToolCallException.class);
    } else {
      return result.getMessage().getBody(ProductPage.class);
    }
  }

  // =========================================================================
  // GET PRODUCT
  // =========================================================================

  @Tool(description = "Get a single product by its ID")
  Product getProduct(
      @ToolArg(description = "Product ID") String productId,
      @ToolArg(description = "User ID for audit logging", required = false) String userId,
      @ToolArg(description = "Client-generated correlation ID for distributed tracing", required = false) String transactionId
  ) {
    log.debug("MCP Tool: getProduct(productId: {})", productId);

    var result = fluentProducerTemplate
        .to("direct:mcpGetProduct")
        .withHeader("productId", productId)
        .withHeader("userId", userId)
        .withHeader("toolName", "getProduct")
        .withHeader("toolParameters", String.format("{\"productId\":\"%s\"}", productId != null ? productId : ""))
        .withHeader("transactionId", transactionId)
        .send();

    if (result.getMessage().getBody() instanceof ToolCallException) {
      throw result.getMessage().getBody(ToolCallException.class);
    } else {
      return result.getMessage().getBody(Product.class);
    }
  }

}
