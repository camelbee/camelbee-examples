package com.mycompany.product.catalog.mapper.api;

import com.mycompany.product.catalog.config.SharedMapperConfig;
import com.mycompany.product.catalog.model.domain.PaginatedResponse;
import com.mycompany.product.catalog.model.domain.Product;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting MCP API Product to Domain Product and vice versa.
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface McpProductMapper {

  // MCP Product to Domain Product
  Product mcpToDomainProduct(com.mycompany.product.catalog.model.api.mcp.Product product);

  // Domain Product to MCP Product
  com.mycompany.product.catalog.model.api.mcp.Product domainToMcpProduct(Product product);

  // List mappings
  List<Product> mcpToDomainProducts(List<com.mycompany.product.catalog.model.api.mcp.Product> products);

  List<com.mycompany.product.catalog.model.api.mcp.Product> domainToMcpProducts(List<Product> products);

  // PaginatedResponse to MCP PaginatedProductResponse
  @Mapping(source = "items", target = "items")
  @Mapping(source = "page", target = "page")
  @Mapping(source = "pageSize", target = "pageSize")
  @Mapping(source = "totalPages", target = "totalPages")
  @Mapping(source = "totalItems", target = "totalItems")
  com.mycompany.product.catalog.model.api.mcp.PaginatedProductResponse domainToMcpPaginatedResponse(PaginatedResponse response);

  // MCP PaginatedProductResponse to Domain PaginatedResponse
  @Mapping(source = "items", target = "items")
  @Mapping(source = "page", target = "page")
  @Mapping(source = "pageSize", target = "pageSize")
  @Mapping(source = "totalPages", target = "totalPages")
  @Mapping(source = "totalItems", target = "totalItems")
  PaginatedResponse mcpToDomainPaginatedResponse(com.mycompany.product.catalog.model.api.mcp.PaginatedProductResponse response);

}
