package com.mycompany.catalog.mcp.mapper.api;

import com.mycompany.catalog.mcp.config.SharedMapperConfig;
import com.mycompany.catalog.mcp.model.domain.Product;
import com.mycompany.catalog.mcp.model.domain.ProductPage;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Api Mcp Product/ProductPage to Domain Product/ProductPage and vice versa.
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface McpProductMapper {

  // Mcp Product to Domain Product
  Product mcpToDomainProduct(com.mycompany.catalog.mcp.model.api.mcp.Product product);

  // Domain Product to Mcp Product
  com.mycompany.catalog.mcp.model.api.mcp.Product domainToMcpProduct(Product product);

  // Product List mappings
  List<Product> mcpToDomainProducts(List<com.mycompany.catalog.mcp.model.api.mcp.Product> products);

  List<com.mycompany.catalog.mcp.model.api.mcp.Product> domainToMcpProducts(List<Product> products);

  // Mcp ProductPage to Domain ProductPage
  @Mapping(source = "products", target = "products")
  ProductPage mcpToDomainProductPage(com.mycompany.catalog.mcp.model.api.mcp.ProductPage productPage);

  // Domain ProductPage to Mcp ProductPage
  @Mapping(source = "products", target = "products")
  com.mycompany.catalog.mcp.model.api.mcp.ProductPage domainToMcpProductPage(ProductPage productPage);

}
