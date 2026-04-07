package com.mycompany.product.catalog.mapper.infra;

import com.mycompany.product.catalog.config.SharedMapperConfig;
import com.mycompany.product.catalog.model.domain.PaginatedResponse;
import com.mycompany.product.catalog.model.domain.Product;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Domain Product to Infra JSON Product and vice versa.
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JsonProductMapper {

  // JSON Product to Domain Product
  Product jsonProductToDomainProduct(com.mycompany.product.catalog.model.infra.json.Product product);

  // Domain Product to JSON Product
  com.mycompany.product.catalog.model.infra.json.Product domainProductToJsonProduct(Product product);

  // List mappings
  List<Product> jsonProductsToDomainProducts(List<com.mycompany.product.catalog.model.infra.json.Product> products);

  List<com.mycompany.product.catalog.model.infra.json.Product> domainProductsToJsonProducts(List<Product> products);

  // JSON PaginatedProductResponse to Domain PaginatedResponse
  @Mapping(source = "items", target = "items")
  @Mapping(source = "page", target = "page")
  @Mapping(source = "pageSize", target = "pageSize")
  @Mapping(source = "totalPages", target = "totalPages")
  @Mapping(source = "totalItems", target = "totalItems")
  PaginatedResponse jsonPaginatedToDomainPaginated(com.mycompany.product.catalog.model.infra.json.PaginatedProductResponse response);

  // Domain PaginatedResponse to JSON PaginatedProductResponse
  @Mapping(source = "items", target = "items")
  @Mapping(source = "page", target = "page")
  @Mapping(source = "pageSize", target = "pageSize")
  @Mapping(source = "totalPages", target = "totalPages")
  @Mapping(source = "totalItems", target = "totalItems")
  com.mycompany.product.catalog.model.infra.json.PaginatedProductResponse domainPaginatedToJsonPaginated(PaginatedResponse response);

}
