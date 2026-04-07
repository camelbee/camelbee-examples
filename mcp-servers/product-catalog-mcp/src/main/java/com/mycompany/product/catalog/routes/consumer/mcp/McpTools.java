package com.mycompany.product.catalog.routes.consumer.mcp;

import com.mycompany.product.catalog.model.api.mcp.PaginatedProductResponse;
import com.mycompany.product.catalog.model.api.mcp.Product;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.FluentProducerTemplate;

/**
 * MCP (Model Context Protocol) Tools for product catalog operations.
 *
 * @author camelbee
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
  PaginatedProductResponse listProducts(
      @ToolArg(description = "Page number", defaultValue = "1") int page,
      @ToolArg(description = "Number of products per page", defaultValue = "10") int pageSize,
      @ToolArg(description = "User ID for audit logging", required = false) String userId,
      @ToolArg(description = "Client-generated correlation ID for distributed tracing", required = false) String transactionId,
      @ToolArg(description = "Business process correlation ID for end-to-end transaction tracing", required = false) String businessTransactionId
  ) {
    log.debug("MCP Tool: listProducts(page: {}, pageSize: {})", page, pageSize);

    var result = fluentProducerTemplate
        .to("direct:mcpListProducts")
        .withHeader("page", page)
        .withHeader("pageSize", pageSize)
        .withHeader("userId", userId)
        .withHeader("transactionId", transactionId)
        .send();

    if (result.getMessage().getBody() instanceof ToolCallException) {
      throw result.getMessage().getBody(ToolCallException.class);
    } else {
      return result.getMessage().getBody(PaginatedProductResponse.class);
    }
  }

  // =========================================================================
  // SEARCH PRODUCTS
  // =========================================================================

  @Tool(description = "Search products with filters: text query, category, price range, stock availability")
  PaginatedProductResponse searchProducts(
      @ToolArg(description = "Text search query", required = false) String query,
      @ToolArg(description = "Category filter", required = false) String category,
      @ToolArg(description = "Minimum price filter", required = false) BigDecimal minPrice,
      @ToolArg(description = "Maximum price filter", required = false) BigDecimal maxPrice,
      @ToolArg(description = "In stock filter", required = false) Boolean inStock,
      @ToolArg(description = "Page number", defaultValue = "1") int page,
      @ToolArg(description = "Number of products per page", defaultValue = "10") int pageSize,
      @ToolArg(description = "User ID for audit logging", required = false) String userId,
      @ToolArg(description = "Client-generated correlation ID for distributed tracing", required = false) String transactionId,
      @ToolArg(description = "Business process correlation ID for end-to-end transaction tracing", required = false) String businessTransactionId
  ) {
    log.debug("MCP Tool: searchProducts(query: {}, category: {}, minPrice: {}, maxPrice: {}, inStock: {}, page: {}, pageSize: {})",
        query, category, minPrice, maxPrice, inStock, page, pageSize);

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
        .withHeader("transactionId", transactionId)
        .send();

    if (result.getMessage().getBody() instanceof ToolCallException) {
      throw result.getMessage().getBody(ToolCallException.class);
    } else {
      return result.getMessage().getBody(PaginatedProductResponse.class);
    }
  }

  // =========================================================================
  // GET PRODUCT
  // =========================================================================

  @Tool(description = "Get a single product by its ID")
  Product getProduct(
      @ToolArg(description = "Product ID") String id,
      @ToolArg(description = "User ID for audit logging", required = false) String userId,
      @ToolArg(description = "Client-generated correlation ID for distributed tracing", required = false) String transactionId,
      @ToolArg(description = "Business process correlation ID for end-to-end transaction tracing", required = false) String businessTransactionId
  ) {
    log.debug("MCP Tool: getProduct(id: {})", id);

    var result = fluentProducerTemplate
        .to("direct:mcpGetProduct")
        .withHeader("productId", id)
        .withHeader("userId", userId)
        .withHeader("transactionId", transactionId)
        .send();

    if (result.getMessage().getBody() instanceof ToolCallException) {
      throw result.getMessage().getBody(ToolCallException.class);
    } else {
      return result.getMessage().getBody(Product.class);
    }
  }

}
