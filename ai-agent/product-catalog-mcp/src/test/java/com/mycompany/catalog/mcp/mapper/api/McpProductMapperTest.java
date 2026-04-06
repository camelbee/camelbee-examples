package com.mycompany.catalog.mcp.mapper.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mycompany.catalog.mcp.model.domain.Product;
import com.mycompany.catalog.mcp.model.domain.ProductPage;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("McpProductMapper Tests")
class McpProductMapperTest {

  private McpProductMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(McpProductMapper.class);
  }

  @Test
  @DisplayName("Should map MCP Product to Domain Product")
  void testMcpToDomainProduct() {
    var mcpProduct = new com.mycompany.catalog.mcp.model.api.mcp.Product();
    mcpProduct.setId("prod-001");
    mcpProduct.setName("Wireless Mouse");
    mcpProduct.setDescription("Ergonomic wireless mouse");
    mcpProduct.setCategory("Electronics");
    mcpProduct.setPrice(new BigDecimal("29.99"));
    mcpProduct.setCurrency("USD");
    mcpProduct.setInStock(true);
    mcpProduct.setImageUrl("https://example.com/mouse.jpg");

    Product result = mapper.mcpToDomainProduct(mcpProduct);

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
  @DisplayName("Should map Domain Product to MCP Product")
  void testDomainToMcpProduct() {
    Product domain = Product.builder()
        .id("prod-001").name("Wireless Mouse").description("Ergonomic").category("Electronics")
        .price(new BigDecimal("29.99")).currency("USD").inStock(true)
        .imageUrl("https://example.com/mouse.jpg").build();

    var result = mapper.domainToMcpProduct(domain);

    assertNotNull(result);
    assertEquals("prod-001", result.getId());
    assertEquals("Wireless Mouse", result.getName());
    assertEquals("Electronics", result.getCategory());
  }

  @Test
  @DisplayName("Should handle null input")
  void testNullInput() {
    assertNull(mapper.mcpToDomainProduct(null));
    assertNull(mapper.domainToMcpProduct(null));
  }

  @Test
  @DisplayName("Should map list of products")
  void testListMapping() {
    Product p1 = Product.builder().id("p1").name("Product 1").build();
    Product p2 = Product.builder().id("p2").name("Product 2").build();

    var result = mapper.domainToMcpProducts(List.of(p1, p2));

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("p1", result.get(0).getId());
    assertEquals("p2", result.get(1).getId());
  }

  @Test
  @DisplayName("Should map ProductPage")
  void testProductPageMapping() {
    ProductPage domainPage = ProductPage.builder()
        .products(List.of(Product.builder().id("p1").name("Product 1").build()))
        .page(1).pageSize(10).totalPages(5).totalItems(47).build();

    var result = mapper.domainToMcpProductPage(domainPage);

    assertNotNull(result);
    assertEquals(1, result.getProducts().size());
    assertEquals(1, result.getPage());
    assertEquals(10, result.getPageSize());
    assertEquals(5, result.getTotalPages());
    assertEquals(47, result.getTotalItems());
  }

}
