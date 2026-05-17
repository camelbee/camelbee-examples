package io.fintech.loan.application.service.mapper.api;

import static org.junit.jupiter.api.Assertions.*;

import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.model.domain.Order.StatusEnum;
import io.fintech.loan.application.service.model.domain.OrderItem;
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
 * Unit tests for {@link JsonOrderMapper} to verify all mapping scenarios work as expected.
 */
class JsonOrderMapperTest {

  private JsonOrderMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(JsonOrderMapper.class);
  }

  @Nested
  @DisplayName("Single Object Mapping Tests")
  class SingleObjectMappingTests {

    @Test
    @DisplayName("Should convert Json Order to Domain Order")
    void testJsonToDomainOrder() {
      // Given
      io.fintech.loan.application.service.model.api.json.OrderItem jsonItem = new io.fintech.loan.application.service.model.api.json.OrderItem();
      jsonItem.setId("item1");
      jsonItem.setProductId("prod1");
      jsonItem.setProductName("Product 1");
      jsonItem.setQuantity(10);
      jsonItem.setPrice(new BigDecimal("99.99"));

      io.fintech.loan.application.service.model.api.json.Order jsonOrder = new io.fintech.loan.application.service.model.api.json.Order();
      jsonOrder.setId("order1");
      jsonOrder.setSalesChannel("Online");
      jsonOrder.setStatus(io.fintech.loan.application.service.model.api.json.Order.StatusEnum.COMPLETED);
      jsonOrder.setOrderDate(LocalDate.of(2025, 2, 26));
      jsonOrder.setLastUpdateTimestamp(OffsetDateTime.of(2025, 2, 26, 14, 30, 0, 0, ZoneOffset.UTC));
      jsonOrder.setItems(Collections.singletonList(jsonItem));

      // When
      Order domainOrder = mapper.jsonToDomainOrder(jsonOrder);

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
    @DisplayName("Should convert Domain Order to Json Order")
    void testDomainToJsonOrder() {
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

      // Use OffsetDateTime for lastUpdateTimestamp
      OffsetDateTime updateTime = OffsetDateTime.of(
          2025, 2, 26, 14, 30, 0, 0,
          ZoneOffset.UTC
      );
      domainOrder.setLastUpdateTimestamp(updateTime);

      domainOrder.setItems(Collections.singletonList(domainItem));

      // When
      io.fintech.loan.application.service.model.api.json.Order jsonOrder = mapper.domainToJsonOrder(domainOrder);

      // Then
      assertNotNull(jsonOrder);
      assertEquals("order1", jsonOrder.getId());
      assertEquals("Online", jsonOrder.getSalesChannel());
      assertEquals(io.fintech.loan.application.service.model.api.json.Order.StatusEnum.COMPLETED, jsonOrder.getStatus());
      assertEquals(LocalDate.of(2025, 2, 26), jsonOrder.getOrderDate());

      // Verify timestamp mapping
      OffsetDateTime expectedDateTime = OffsetDateTime.of(2025, 2, 26, 14, 30, 0, 0, ZoneOffset.UTC);
      assertEquals(expectedDateTime, jsonOrder.getLastUpdateTimestamp());

      // And item is correctly mapped
      assertNotNull(jsonOrder.getItems());
      assertEquals(1, jsonOrder.getItems().size());
      io.fintech.loan.application.service.model.api.json.OrderItem mappedItem = jsonOrder.getItems().get(0);
      assertEquals("item1", mappedItem.getId());
      assertEquals("prod1", mappedItem.getProductId());
      assertEquals("Product 1", mappedItem.getProductName());
      assertEquals(10, mappedItem.getQuantity());
      assertEquals(new BigDecimal("99.99"), mappedItem.getPrice());
    }

    @Test
    @DisplayName("Should convert Json OrderItem to Domain OrderItem")
    void testJsonToDomainOrderItem() {
      // Given
      io.fintech.loan.application.service.model.api.json.OrderItem jsonItem = new io.fintech.loan.application.service.model.api.json.OrderItem();
      jsonItem.setId("item1");
      jsonItem.setProductId("prod1");
      jsonItem.setProductName("Product 1");
      jsonItem.setQuantity(10);
      jsonItem.setPrice(new BigDecimal("99.99"));

      // When
      OrderItem domainItem = mapper.jsonToDomainOrderItem(jsonItem);

      // Then
      assertNotNull(domainItem);
      assertEquals("item1", domainItem.getId());
      assertEquals("prod1", domainItem.getProductId());
      assertEquals("Product 1", domainItem.getProductName());
      assertEquals(10, domainItem.getQuantity());
      assertEquals(99.99, domainItem.getPrice().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Should convert Domain OrderItem to Json OrderItem")
    void testDomainToJsonOrderItem() {
      // Given
      OrderItem domainItem = new OrderItem();
      domainItem.setId("item1");
      domainItem.setProductId("prod1");
      domainItem.setProductName("Product 1");
      domainItem.setQuantity(10);
      domainItem.setPrice(new BigDecimal("99.99"));

      // When
      io.fintech.loan.application.service.model.api.json.OrderItem jsonItem = mapper.domainToJsonOrderItem(domainItem);

      // Then
      assertNotNull(jsonItem);
      assertEquals("item1", jsonItem.getId());
      assertEquals("prod1", jsonItem.getProductId());
      assertEquals("Product 1", jsonItem.getProductName());
      assertEquals(10, jsonItem.getQuantity());
      assertEquals(new BigDecimal("99.99"), jsonItem.getPrice());
    }
  }

  @Nested
  @DisplayName("List Mapping Tests")
  class ListMappingTests {

    @Test
    @DisplayName("Should convert list of Json Orders to Domain Orders")
    void testJsonToDomainOrders() {
      // Given
      io.fintech.loan.application.service.model.api.json.OrderItem jsonItem = new io.fintech.loan.application.service.model.api.json.OrderItem();
      jsonItem.setId("item1");
      jsonItem.setProductId("prod1");
      jsonItem.setProductName("Product 1");
      jsonItem.setQuantity(10);
      jsonItem.setPrice(new BigDecimal("99.99"));

      io.fintech.loan.application.service.model.api.json.Order jsonOrder = new io.fintech.loan.application.service.model.api.json.Order();
      jsonOrder.setId("order1");
      jsonOrder.setSalesChannel("Online");
      jsonOrder.setStatus(io.fintech.loan.application.service.model.api.json.Order.StatusEnum.COMPLETED);
      jsonOrder.setOrderDate(LocalDate.of(2025, 2, 26));
      jsonOrder.setLastUpdateTimestamp(OffsetDateTime.of(2025, 2, 26, 14, 30, 0, 0, ZoneOffset.UTC));
      jsonOrder.setItems(Collections.singletonList(jsonItem));

      List<io.fintech.loan.application.service.model.api.json.Order> jsonOrders = Arrays.asList(jsonOrder, jsonOrder);

      // When
      List<Order> domainOrders = mapper.jsonToDomainOrders(jsonOrders);

      // Then
      assertNotNull(domainOrders);
      assertEquals(2, domainOrders.size());

      // Verify first item
      Order firstOrder = domainOrders.get(0);
      assertEquals("order1", firstOrder.getId());
      assertEquals(Order.StatusEnum.COMPLETED, firstOrder.getStatus());
    }

    @Test
    @DisplayName("Should convert list of Domain Orders to Json Orders")
    void testDomainToJsonOrders() {
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

      // Use OffsetDateTime for lastUpdateTimestamp
      OffsetDateTime updateTime = OffsetDateTime.of(
          2025, 2, 26, 14, 30, 0, 0,
          ZoneOffset.UTC
      );
      domainOrder.setLastUpdateTimestamp(updateTime);

      domainOrder.setItems(Collections.singletonList(domainItem));

      List<Order> domainOrders = Arrays.asList(domainOrder, domainOrder);

      // When
      List<io.fintech.loan.application.service.model.api.json.Order> jsonOrders = mapper.domainToJsonOrders(domainOrders);

      // Then
      assertNotNull(jsonOrders);
      assertEquals(2, jsonOrders.size());

      // Verify first item
      io.fintech.loan.application.service.model.api.json.Order firstOrder = jsonOrders.get(0);
      assertEquals("order1", firstOrder.getId());
      assertEquals(io.fintech.loan.application.service.model.api.json.Order.StatusEnum.COMPLETED, firstOrder.getStatus());
    }

    @Test
    @DisplayName("Should convert list of Json OrderItems to Domain OrderItems")
    void testJsonToDomainOrderItems() {
      // Given
      io.fintech.loan.application.service.model.api.json.OrderItem jsonItem1 = new io.fintech.loan.application.service.model.api.json.OrderItem();
      jsonItem1.setId("item1");
      jsonItem1.setProductId("prod1");
      jsonItem1.setProductName("Product 1");
      jsonItem1.setQuantity(10);
      jsonItem1.setPrice(new BigDecimal("99.99"));

      io.fintech.loan.application.service.model.api.json.OrderItem jsonItem2 = new io.fintech.loan.application.service.model.api.json.OrderItem();
      jsonItem2.setId("item2");
      jsonItem2.setProductId("prod2");
      jsonItem2.setProductName("Product 2");
      jsonItem2.setQuantity(5);
      jsonItem2.setPrice(new BigDecimal("49.99"));

      List<io.fintech.loan.application.service.model.api.json.OrderItem> jsonItems = Arrays.asList(jsonItem1, jsonItem2);

      // When
      List<OrderItem> domainItems = mapper.jsonToDomainOrderItems(jsonItems);

      // Then
      assertNotNull(domainItems);
      assertEquals(2, domainItems.size());
      assertEquals("item1", domainItems.get(0).getId());
      assertEquals("item2", domainItems.get(1).getId());
    }

    @Test
    @DisplayName("Should convert list of Domain OrderItems to Json OrderItems")
    void testDomainToJsonOrderItems() {
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
      List<io.fintech.loan.application.service.model.api.json.OrderItem> jsonItems = mapper.domainToJsonOrderItems(domainItems);

      // Then
      assertNotNull(jsonItems);
      assertEquals(2, jsonItems.size());
      assertEquals("item1", jsonItems.get(0).getId());
      assertEquals("item2", jsonItems.get(1).getId());
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