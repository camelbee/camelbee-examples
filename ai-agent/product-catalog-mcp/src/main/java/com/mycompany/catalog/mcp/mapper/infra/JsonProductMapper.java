package com.mycompany.catalog.mcp.mapper.infra;

import com.mycompany.catalog.mcp.config.SharedMapperConfig;
import com.mycompany.catalog.mcp.model.domain.Product;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Domain Product to Infra Json Product and vice versa.
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JsonProductMapper {

  // Json Product to Domain Product
  Product jsonProductToDomainProduct(com.mycompany.catalog.mcp.model.infra.json.Product product);

  // Domain Product to Json Product
  com.mycompany.catalog.mcp.model.infra.json.Product domainProductToJsonProduct(Product product);

  // Product List mappings
  List<Product> jsonProductsToDomainProducts(List<com.mycompany.catalog.mcp.model.infra.json.Product> products);

  List<com.mycompany.catalog.mcp.model.infra.json.Product> domainProductsToJsonProducts(List<Product> products);

}
