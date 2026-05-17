package com.mycompany.product.catalog.mapper.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.product.catalog.model.domain.PaginatedResponse;
import com.mycompany.product.catalog.model.domain.Product;
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
  void test_JsonToDomain_Product() {
    var json = new com.mycompany.product.catalog.model.infra.json.Product();
    json.setId("prod-001");
    json.setName("Wireless Mouse");
    json.setDescription("Ergonomic wireless mouse");
    json.setCategory("Electronics");
    json.setPrice(BigDecimal.valueOf(29.99));
    json.setCurrency("USD");
    json.setInStock(true);
    json.setImageUrl("https://example.com/mouse.jpg");

    Product domain = mapper.jsonProductToDomainProduct(json);

    assertThat(domain).isNotNull();
    assertThat(domain.getId()).isEqualTo("prod-001");
    assertThat(domain.getName()).isEqualTo("Wireless Mouse");
    assertThat(domain.getCategory()).isEqualTo("Electronics");
    assertThat(domain.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(29.99));
    assertThat(domain.getCurrency()).isEqualTo("USD");
    assertThat(domain.getInStock()).isTrue();
    assertThat(domain.getImageUrl()).isEqualTo("https://example.com/mouse.jpg");
  }

  @Test
  @DisplayName("Should map Domain Product to JSON Product")
  void test_DomainToJson_Product() {
    Product domain = Product.builder()
        .id("prod-002")
        .name("USB Keyboard")
        .description("Mechanical USB keyboard")
        .category("Electronics")
        .price(BigDecimal.valueOf(79.99))
        .currency("USD")
        .inStock(false)
        .imageUrl("https://example.com/keyboard.jpg")
        .build();

    var json = mapper.domainProductToJsonProduct(domain);

    assertThat(json).isNotNull();
    assertThat(json.getId()).isEqualTo("prod-002");
    assertThat(json.getName()).isEqualTo("USB Keyboard");
    assertThat(json.getInStock()).isFalse();
  }

  @Test
  @DisplayName("Should handle null input")
  void test_NullInput() {
    assertThat(mapper.jsonProductToDomainProduct(null)).isNull();
    assertThat(mapper.domainProductToJsonProduct(null)).isNull();
  }

  @Test
  @DisplayName("Should map list of products")
  void test_ListMapping() {
    var json1 = new com.mycompany.product.catalog.model.infra.json.Product();
    json1.setId("prod-001");
    json1.setName("Mouse");
    var json2 = new com.mycompany.product.catalog.model.infra.json.Product();
    json2.setId("prod-002");
    json2.setName("Keyboard");

    List<Product> domainList = mapper.jsonProductsToDomainProducts(List.of(json1, json2));

    assertThat(domainList).hasSize(2);
    assertThat(domainList.get(0).getId()).isEqualTo("prod-001");
    assertThat(domainList.get(1).getId()).isEqualTo("prod-002");
  }

  @Test
  @DisplayName("Should map PaginatedProductResponse to Domain PaginatedResponse")
  void test_JsonPaginatedToDomain() {
    var jsonProduct = new com.mycompany.product.catalog.model.infra.json.Product();
    jsonProduct.setId("prod-001");
    jsonProduct.setName("Mouse");

    var jsonPaginated = new com.mycompany.product.catalog.model.infra.json.PaginatedProductResponse();
    jsonPaginated.setItems(List.of(jsonProduct));
    jsonPaginated.setPage(1);
    jsonPaginated.setPageSize(10);
    jsonPaginated.setTotalPages(5);
    jsonPaginated.setTotalItems(42);

    PaginatedResponse domain = mapper.jsonPaginatedToDomainPaginated(jsonPaginated);

    assertThat(domain).isNotNull();
    assertThat(domain.getItems()).hasSize(1);
    assertThat(domain.getItems().get(0).getId()).isEqualTo("prod-001");
    assertThat(domain.getPage()).isEqualTo(1);
    assertThat(domain.getPageSize()).isEqualTo(10);
    assertThat(domain.getTotalPages()).isEqualTo(5);
    assertThat(domain.getTotalItems()).isEqualTo(42);
  }

  @Test
  @DisplayName("Should map Domain PaginatedResponse to JSON PaginatedProductResponse")
  void test_DomainPaginatedToJson() {
    PaginatedResponse domain = PaginatedResponse.builder()
        .items(List.of(Product.builder().id("prod-001").name("Mouse").build()))
        .page(2).pageSize(5).totalPages(3).totalItems(15)
        .build();

    var json = mapper.domainPaginatedToJsonPaginated(domain);

    assertThat(json).isNotNull();
    assertThat(json.getItems()).hasSize(1);
    assertThat(json.getPage()).isEqualTo(2);
    assertThat(json.getTotalItems()).isEqualTo(15);
  }
}
