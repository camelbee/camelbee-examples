package com.mycompany.product.catalog.mapper.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.product.catalog.model.domain.PaginatedResponse;
import com.mycompany.product.catalog.model.domain.Product;
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
  void test_McpToDomain_Product() {
    var mcpProduct = new com.mycompany.product.catalog.model.api.mcp.Product();
    mcpProduct.setId("prod-001");
    mcpProduct.setName("Wireless Mouse");
    mcpProduct.setDescription("Ergonomic wireless mouse");
    mcpProduct.setCategory("Electronics");
    mcpProduct.setPrice(BigDecimal.valueOf(29.99));
    mcpProduct.setCurrency("USD");
    mcpProduct.setInStock(true);
    mcpProduct.setImageUrl("https://example.com/mouse.jpg");

    Product domainProduct = mapper.mcpToDomainProduct(mcpProduct);

    assertThat(domainProduct).isNotNull();
    assertThat(domainProduct.getId()).isEqualTo("prod-001");
    assertThat(domainProduct.getName()).isEqualTo("Wireless Mouse");
    assertThat(domainProduct.getCategory()).isEqualTo("Electronics");
    assertThat(domainProduct.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(29.99));
    assertThat(domainProduct.getInStock()).isTrue();
  }

  @Test
  @DisplayName("Should map Domain Product to MCP Product")
  void test_DomainToMcp_Product() {
    Product domainProduct = Product.builder()
        .id("prod-001")
        .name("Wireless Mouse")
        .description("Ergonomic wireless mouse")
        .category("Electronics")
        .price(BigDecimal.valueOf(29.99))
        .currency("USD")
        .inStock(true)
        .imageUrl("https://example.com/mouse.jpg")
        .build();

    var mcpProduct = mapper.domainToMcpProduct(domainProduct);

    assertThat(mcpProduct).isNotNull();
    assertThat(mcpProduct.getId()).isEqualTo("prod-001");
    assertThat(mcpProduct.getName()).isEqualTo("Wireless Mouse");
    assertThat(mcpProduct.getInStock()).isTrue();
  }

  @Test
  @DisplayName("Should handle null input")
  void test_NullInput() {
    assertThat(mapper.mcpToDomainProduct(null)).isNull();
    assertThat(mapper.domainToMcpProduct(null)).isNull();
  }

  @Test
  @DisplayName("Should map PaginatedResponse")
  void test_PaginatedResponse() {
    PaginatedResponse domainResponse = PaginatedResponse.builder()
        .items(List.of(Product.builder().id("prod-001").name("Mouse").build()))
        .page(1).pageSize(10).totalPages(1).totalItems(1)
        .build();

    var mcpResponse = mapper.domainToMcpPaginatedResponse(domainResponse);

    assertThat(mcpResponse).isNotNull();
    assertThat(mcpResponse.getItems()).hasSize(1);
    assertThat(mcpResponse.getPage()).isEqualTo(1);
    assertThat(mcpResponse.getTotalItems()).isEqualTo(1);
  }
}
