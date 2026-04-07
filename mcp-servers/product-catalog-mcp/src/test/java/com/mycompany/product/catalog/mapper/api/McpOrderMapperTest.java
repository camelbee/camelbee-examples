package com.mycompany.product.catalog.mapper.api;

import static org.junit.jupiter.api.Assertions.*;

import com.mycompany.product.catalog.model.domain.Order;
import com.mycompany.product.catalog.model.domain.Order.StatusEnum;
import com.mycompany.product.catalog.model.domain.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for {@link McpOrderMapper} to verify all mapping scenarios work as expected.
 */
class McpOrderMapperTest {

  private McpOrderMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(McpOrderMapper.class);
  }

  @Nested
  @DisplayName("Single Object Mapping Tests")
  class SingleObjectMappingTests {

    @Test
    @DisplayName("Should convert Mcp Order to Domain Order")
    void testMcpToDomainOrder() {
      // Given
      com.mycompany.product.catalog.model.api.mcp.OrderItem mcpItem = new com.mycompany.product.catalog.model.api.mcp.OrderItem();
      mcpItem.setId("item1");
      mcpItem.setProductId("prod1");
      mcpItem.setProductName("Product 1");
      mcpItem.setQuantity(10);
      mcpItem.setPrice(new BigDecimal("99.99"));

      com.mycompany.product.catalog.model.api.mcp.Order mcpOrder = new com.mycompany.product.catalog.model.api.mcp.Order();
      mcpOrder.setId("order1");
      mcpOrder.setSalesChannel("Online");
      mcpOrder.setStatus(com.mycompany.product.catalog.model.api.mcp.Order.StatusEnum.COMPLETED);
      mcpOrder.setOrderDate(LocalDate.of(2025, 2, 26));
      mcpOrder.setLastUpdateTimestamp(OffsetDateTime.of(2025, 2, 26, 14, 30, 0, 0, ZoneOffset.UTC));
      mcpOrder.setItems(Collections.singletonList(mcpItem));

      // When
      Order domainOrder = mapper.mcpToDomainOrder(mcpOrder);

      // Then
      assertNotNull(domainOrder);
      assertEquals("order1", domainOrder.getId());
      assertEquals("Online", domainOrder.getSalesChannel());
      assertEquals(Order.StatusEnum.COMPLETED, domainOrder.getStatus());
      assertEquals(LocalDate.of(2025, 2, 26), domainOrder.getOrderDate());

      // Verify timestamp conversion
      OffsetDateTime expectedDateTime = LocalDateTime.of(2025, 2, 26, 14, 30, 0)
          .atOffset(ZoneOffset.UTC);
      assertEquals(expectedDateTime.getHour(), domainOrder.getLastUpdateTimestamp().getHour());
      assertEquals(expectedDateTime.getMinute(), domainOrder.getLastUpdateTimestamp().getMinute());

      // And item is correctly mapped
      assertNotNull(domainOrder.getItems());
      assertEquals(1, domainOrder.getItems().size());
      OrderItem mappedItem = domainOrder.getItems().get(0);
      assertEquals("item1", mappedItem.getId());
      assertEquals("prod1", mappedItem.getProductId());
      assertEquals("Product 1", mappedItem.getProductName());
      assertEquals(10, mappedItem.getQuantity());
      assertEquals(99.99, mappedItem.getPrice().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Should convert Domain Order to Mcp Order")
    void testDomainToMcpOrder() {
      // Given
      OrderItem domainItem = new OrderItem();
      domainItem.setId("item1");
      domainItem.setProductId("prod1");
      domainItem.setProductName("Product 1");
      domainItem.setQuantity(10);
      domainItem.setPrice(new BigDecimal("99.99"));

      Order domainOrder = new Order();
      domainOrder.setId("order1");
      domainOrder.setSalesChannel("Online");
      domainOrder.setStatus(Order.StatusEnum.COMPLETED);
      domainOrder.setOrderDate(LocalDate.of(2025, 2, 26));

      OffsetDateTime updateTime = OffsetDateTime.of(
          2025, 2, 26, 14, 30, 0, 0,
          ZoneOffset.UTC
      );
      domainOrder.setLastUpdateTimestamp(updateTime);

      domainOrder.setItems(Collections.singletonList(domainItem));

      // When
      com.mycompany.product.catalog.model.api.mcp.Order mcpOrder = mapper.domainToMcpOrder(domainOrder);

      // Then
      assertNotNull(mcpOrder);
      assertEquals("order1", mcpOrder.getId());
      assertEquals("Online", mcpOrder.getSalesChannel());
      assertEquals(com.mycompany.product.catalog.model.api.mcp.Order.StatusEnum.COMPLETED, mcpOrder.getStatus());
      assertEquals(LocalDate.of(2025, 2, 26), mcpOrder.getOrderDate());

      // Verify timestamp mapping
      assertNotNull(mcpOrder.getLastUpdateTimestamp());

      // And item is correctly mapped
      assertNotNull(mcpOrder.getItems());
      assertEquals(1, mcpOrder.getItems().size());
      com.mycompany.product.catalog.model.api.mcp.OrderItem mappedItem = mcpOrder.getItems().get(0);
      assertEquals("item1", mappedItem.getId());
      assertEquals("prod1", mappedItem.getProductId());
      assertEquals("Product 1", mappedItem.getProductName());
      assertEquals(10, mappedItem.getQuantity());
      assertEquals(new BigDecimal("99.99"), mappedItem.getPrice());
    }

    @Test
    @DisplayName("Should convert Mcp OrderItem to Domain OrderItem")
    void testMcpToDomainOrderItem() {
      // Given
      com.mycompany.product.catalog.model.api.mcp.OrderItem mcpItem = new com.mycompany.product.catalog.model.api.mcp.OrderItem();
      mcpItem.setId("item1");
      mcpItem.setProductId("prod1");
      mcpItem.setProductName("Product 1");
      mcpItem.setQuantity(10);
      mcpItem.setPrice(new BigDecimal("99.99"));

      // When
      OrderItem domainItem = mapper.mcpToDomainOrderItem(mcpItem);

      // Then
      assertNotNull(domainItem);
      assertEquals("item1", domainItem.getId());
      assertEquals("prod1", domainItem.getProductId());
      assertEquals("Product 1", domainItem.getProductName());
      assertEquals(10, domainItem.getQuantity());
      assertEquals(99.99, domainItem.getPrice().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Should convert Domain OrderItem to Mcp OrderItem")
    void testDomainToMcpOrderItem() {
      // Given
      OrderItem domainItem = new OrderItem();
      domainItem.setId("item1");
      domainItem.setProductId("prod1");
      domainItem.setProductName("Product 1");
      domainItem.setQuantity(10);
      domainItem.setPrice(new BigDecimal("99.99"));

      // When
      com.mycompany.product.catalog.model.api.mcp.OrderItem mcpItem = mapper.domainToMcpOrderItem(domainItem);

      // Then
      assertNotNull(mcpItem);
      assertEquals("item1", mcpItem.getId());
      assertEquals("prod1", mcpItem.getProductId());
      assertEquals("Product 1", mcpItem.getProductName());
      assertEquals(10, mcpItem.getQuantity());
      assertEquals(new BigDecimal("99.99"), mcpItem.getPrice());
    }
  }

  @Nested
  @DisplayName("List Mapping Tests")
  class ListMappingTests {

    @Test
    @DisplayName("Should convert list of Mcp Orders to Domain Orders")
    void testMcpToDomainOrders() {
      // Given
      com.mycompany.product.catalog.model.api.mcp.OrderItem mcpItem = new com.mycompany.product.catalog.model.api.mcp.OrderItem();
      mcpItem.setId("item1");
      mcpItem.setProductId("prod1");
      mcpItem.setProductName("Product 1");
      mcpItem.setQuantity(10);
      mcpItem.setPrice(new BigDecimal("99.99"));

      com.mycompany.product.catalog.model.api.mcp.Order mcpOrder = new com.mycompany.product.catalog.model.api.mcp.Order();
      mcpOrder.setId("order1");
      mcpOrder.setSalesChannel("Online");
      mcpOrder.setStatus(com.mycompany.product.catalog.model.api.mcp.Order.StatusEnum.COMPLETED);
      mcpOrder.setOrderDate(LocalDate.of(2025, 2, 26));
      mcpOrder.setLastUpdateTimestamp(OffsetDateTime.of(2025, 2, 26, 14, 30, 0, 0, ZoneOffset.UTC));
      mcpOrder.setItems(Collections.singletonList(mcpItem));

      List<com.mycompany.product.catalog.model.api.mcp.Order> mcpOrders = Arrays.asList(mcpOrder, mcpOrder);

      // When
      List<Order> domainOrders = mapper.mcpToDomainOrders(mcpOrders);

      // Then
      assertNotNull(domainOrders);
      assertEquals(2, domainOrders.size());

      Order firstOrder = domainOrders.get(0);
      assertEquals("order1", firstOrder.getId());
      assertEquals(Order.StatusEnum.COMPLETED, firstOrder.getStatus());
    }

    @Test
    @DisplayName("Should convert list of Domain Orders to Mcp Orders")
    void testDomainToMcpOrders() {
      // Given
      OrderItem domainItem = new OrderItem();
      domainItem.setId("item1");
      domainItem.setProductId("prod1");
      domainItem.setProductName("Product 1");
      domainItem.setQuantity(10);
      domainItem.setPrice(new BigDecimal("99.99"));

      Order domainOrder = new Order();
      domainOrder.setId("order1");
      domainOrder.setSalesChannel("Online");
      domainOrder.setStatus(StatusEnum.COMPLETED);
      domainOrder.setOrderDate(LocalDate.of(2025, 2, 26));

      OffsetDateTime updateTime = OffsetDateTime.of(
          2025, 2, 26, 14, 30, 0, 0,
          ZoneOffset.UTC
      );
      domainOrder.setLastUpdateTimestamp(updateTime);

      domainOrder.setItems(Collections.singletonList(domainItem));

      List<Order> domainOrders = Arrays.asList(domainOrder, domainOrder);

      // When
      List<com.mycompany.product.catalog.model.api.mcp.Order> mcpOrders = mapper.domainToMcpOrders(domainOrders);

      // Then
      assertNotNull(mcpOrders);
      assertEquals(2, mcpOrders.size());

      com.mycompany.product.catalog.model.api.mcp.Order firstOrder = mcpOrders.get(0);
      assertEquals("order1", firstOrder.getId());
      assertEquals(com.mycompany.product.catalog.model.api.mcp.Order.StatusEnum.COMPLETED, firstOrder.getStatus());
    }

    @Test
    @DisplayName("Should convert list of Mcp OrderItems to Domain OrderItems")
    void testMcpToDomainOrderItems() {
      // Given
      com.mycompany.product.catalog.model.api.mcp.OrderItem mcpItem1 = new com.mycompany.product.catalog.model.api.mcp.OrderItem();
      mcpItem1.setId("item1");
      mcpItem1.setProductId("prod1");
      mcpItem1.setProductName("Product 1");
      mcpItem1.setQuantity(10);
      mcpItem1.setPrice(new BigDecimal("99.99"));

      com.mycompany.product.catalog.model.api.mcp.OrderItem mcpItem2 = new com.mycompany.product.catalog.model.api.mcp.OrderItem();
      mcpItem2.setId("item2");
      mcpItem2.setProductId("prod2");
      mcpItem2.setProductName("Product 2");
      mcpItem2.setQuantity(5);
      mcpItem2.setPrice(new BigDecimal("49.99"));

      List<com.mycompany.product.catalog.model.api.mcp.OrderItem> mcpItems = Arrays.asList(mcpItem1, mcpItem2);

      // When
      List<OrderItem> domainItems = mapper.mcpToDomainOrderItems(mcpItems);

      // Then
      assertNotNull(domainItems);
      assertEquals(2, domainItems.size());
      assertEquals("item1", domainItems.get(0).getId());
      assertEquals("item2", domainItems.get(1).getId());
    }

    @Test
    @DisplayName("Should convert list of Domain OrderItems to Mcp OrderItems")
    void testDomainToMcpOrderItems() {
      // Given
      OrderItem domainItem1 = new OrderItem();
      domainItem1.setId("item1");
      domainItem1.setProductId("prod1");
      domainItem1.setProductName("Product 1");
      domainItem1.setQuantity(10);
      domainItem1.setPrice(new BigDecimal("99.99"));

      OrderItem domainItem2 = new OrderItem();
      domainItem2.setId("item2");
      domainItem2.setProductId("prod2");
      domainItem2.setProductName("Product 2");
      domainItem2.setQuantity(5);
      domainItem2.setPrice(new BigDecimal("49.99"));

      List<OrderItem> domainItems = Arrays.asList(domainItem1, domainItem2);

      // When
      List<com.mycompany.product.catalog.model.api.mcp.OrderItem> mcpItems = mapper.domainToMcpOrderItems(domainItems);

      // Then
      assertNotNull(mcpItems);
      assertEquals(2, mcpItems.size());
      assertEquals("item1", mcpItems.get(0).getId());
      assertEquals("item2", mcpItems.get(1).getId());
    }
  }

  @Nested
  @DisplayName("Custom Mapping Tests")
  class CustomMappingTests {

    @Test
    @DisplayName("Should map LocalDateTime and OffsetDateTime correctly")
    void testDateTimeMapping() {
      // Given
      LocalDateTime localDateTime = LocalDateTime.of(2025, 2, 26, 14, 30, 0);
      OffsetDateTime offsetDateTime = OffsetDateTime.of(
          2025, 2, 26, 14, 30, 0, 0,
          ZoneOffset.UTC
      );

      // When & Then
      assertEquals(offsetDateTime, mapper.map(localDateTime));
      assertEquals(localDateTime, mapper.map(offsetDateTime));

      // Handle null case
      assertNull(mapper.map((LocalDateTime) null));
      assertNull(mapper.map((OffsetDateTime) null));
    }
  }
}
