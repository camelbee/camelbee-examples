package com.mycompany.catalog.mcp.mapper.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mycompany.catalog.mcp.model.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("JsonProductMapper Tests")
class JsonProductMapperTest {

  private JsonProductMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(JsonProductMapper.class);
  }

  @Test
  @DisplayName("Should map JSON Product to Domain Product")
  void jsonProductToDomainProduct_shouldMapAllFields() {
    var jsonProduct = new com.mycompany.catalog.mcp.model.infra.json.Product();
    jsonProduct.setId("prod-001");
    jsonProduct.setName("Wireless Mouse");
    jsonProduct.setDescription("Ergonomic wireless mouse");
    jsonProduct.setCategory("Electronics");
    jsonProduct.setPrice(new BigDecimal("29.99"));
    jsonProduct.setCurrency("USD");
    jsonProduct.setInStock(true);
    jsonProduct.setImageUrl("https://example.com/mouse.jpg");

    Product result = mapper.jsonProductToDomainProduct(jsonProduct);

    assertNotNull(result);
    assertEquals("prod-001", result.getId());
    assertEquals("Wireless Mouse", result.getName());
    assertEquals("Ergonomic wireless mouse", result.getDescription());
    assertEquals("Electronics", result.getCategory());
    assertEquals(new BigDecimal("29.99"), result.getPrice());
    assertEquals("USD", result.getCurrency());
    assertEquals(true, result.getInStock());
    assertEquals("https://example.com/mouse.jpg", result.getImageUrl());
  }

  @Test
  @DisplayName("Should map Domain Product to JSON Product")
  void domainProductToJsonProduct_shouldMapAllFields() {
    Product domain = Product.builder()
        .id("prod-001").name("Wireless Mouse").description("Ergonomic")
        .category("Electronics").price(new BigDecimal("29.99"))
        .currency("USD").inStock(true).imageUrl("https://example.com/mouse.jpg")
        .build();

    var result = mapper.domainProductToJsonProduct(domain);

    assertNotNull(result);
    assertEquals("prod-001", result.getId());
    assertEquals("Wireless Mouse", result.getName());
  }

  @Test
  @DisplayName("Should handle null input")
  void nullInput_shouldReturnNull() {
    assertNull(mapper.jsonProductToDomainProduct(null));
    assertNull(mapper.domainProductToJsonProduct(null));
  }

  @Test
  @DisplayName("Should map list of products")
  void jsonProductsToDomainProducts_shouldMapAllProducts() {
    var p1 = new com.mycompany.catalog.mcp.model.infra.json.Product();
    p1.setId("p1");
    p1.setName("Product 1");
    var p2 = new com.mycompany.catalog.mcp.model.infra.json.Product();
    p2.setId("p2");
    p2.setName("Product 2");

    List<Product> result = mapper.jsonProductsToDomainProducts(List.of(p1, p2));

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("p1", result.get(0).getId());
    assertEquals("p2", result.get(1).getId());
  }

}
