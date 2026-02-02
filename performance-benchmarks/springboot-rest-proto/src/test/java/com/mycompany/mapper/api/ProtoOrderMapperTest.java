package com.mycompany.mapper.api;

import static org.junit.jupiter.api.Assertions.*;

import com.google.protobuf.Timestamp;
import com.google.type.Date;
import com.mycompany.model.domain.Order;
import com.mycompany.model.domain.Order.StatusEnum;
import com.mycompany.model.domain.OrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
 * Unit tests for {@link ProtoOrderMapper} to verify all mapping scenarios work as expected.
 */
class ProtoOrderMapperTest {

  private ProtoOrderMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(ProtoOrderMapper.class);
  }

  @Nested
  @DisplayName("Single Object Mapping Tests")
  class SingleObjectMappingTests {

    @Test
    @DisplayName("Should convert Proto Order to Domain Order")
    void testProtoToDomainOrder() {
      // Given
      com.mycompany.model.api.proto.OrderItem protoItem = com.mycompany.model.api.proto.OrderItem.newBuilder()
          .setId("item1")
          .setProductId("prod1")
          .setProductName("Product 1")
          .setQuantity(10)
          .setPrice(99.99)
          .build();

      Date orderDate = Date.newBuilder()
          .setYear(2025)
          .setMonth(2)
          .setDay(26)
          .build();

      Timestamp lastUpdateTimestamp = Timestamp.newBuilder()
          .setSeconds(1616161616L)
          .setNanos(161000000)
          .build();

      com.mycompany.model.api.proto.Order protoOrder = com.mycompany.model.api.proto.Order.newBuilder()
          .setId("order1")
          .setSalesChannel("Online")
          .setStatus("COMPLETED")
          .setOrderDate(orderDate)
          .setLastUpdateTimestamp(lastUpdateTimestamp)
          .addItems(protoItem)
          .build();

      // When
      Order domainOrder = mapper.protoToDomainOrder(protoOrder);

      // Then
      assertNotNull(domainOrder);
      assertEquals("order1", domainOrder.getId());
      assertEquals("Online", domainOrder.getSalesChannel());
      assertEquals(Order.StatusEnum.COMPLETED, domainOrder.getStatus());
      assertEquals(LocalDate.of(2025, 2, 26), domainOrder.getOrderDate());

      // Compare timestamps by epoch seconds to avoid timezone issues
      Instant expectedInstant = Instant.ofEpochSecond(1616161616L, 161000000);
      assertEquals(expectedInstant.getEpochSecond(),
          domainOrder.getLastUpdateTimestamp().toInstant().getEpochSecond());

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
    @DisplayName("Should convert Domain Order to Proto Order")
    void testDomainToProtoOrder() {
      // Given
      OrderItem domainItem = new OrderItem();
      domainItem.setId("item1");
      domainItem.setProductId("prod1");
      domainItem.setProductName("Product 1");
      domainItem.setQuantity(10);
      domainItem.setPrice(new BigDecimal(99.99));

      Order domainOrder = new Order();
      domainOrder.setId("order1");
      domainOrder.setSalesChannel("Online");
      domainOrder.setStatus(Order.StatusEnum.COMPLETED);
      domainOrder.setOrderDate(LocalDate.of(2025, 2, 26));

      // Use OffsetDateTime for lastUpdateTimestamp
      OffsetDateTime updateTime = OffsetDateTime.ofInstant(
          Instant.ofEpochSecond(1616161616L, 161000000),
          ZoneOffset.UTC
      );
      domainOrder.setLastUpdateTimestamp(updateTime);

      domainOrder.setItems(Collections.singletonList(domainItem));

      // When
      com.mycompany.model.api.proto.Order protoOrder = mapper.domainToProtoOrder(domainOrder);

      // Then
      assertNotNull(protoOrder);
      assertEquals("order1", protoOrder.getId());
      assertEquals("Online", protoOrder.getSalesChannel());
      assertEquals("COMPLETED", protoOrder.getStatus());

      // Verify date mapping
      assertEquals(2025, protoOrder.getOrderDate().getYear());
      assertEquals(2, protoOrder.getOrderDate().getMonth());
      assertEquals(26, protoOrder.getOrderDate().getDay());

      // Verify timestamp mapping - compare seconds and nanos
      assertEquals(1616161616L, protoOrder.getLastUpdateTimestamp().getSeconds());
      assertEquals(161000000, protoOrder.getLastUpdateTimestamp().getNanos());

      // And item is correctly mapped
      assertNotNull(protoOrder.getItemsList());
      assertEquals(1, protoOrder.getItemsList().size());
      com.mycompany.model.api.proto.OrderItem mappedItem = protoOrder.getItemsList().get(0);
      assertEquals("item1", mappedItem.getId());
      assertEquals("prod1", mappedItem.getProductId());
      assertEquals("Product 1", mappedItem.getProductName());
      assertEquals(10, mappedItem.getQuantity());
      assertEquals(99.99, mappedItem.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Should convert Proto OrderItem to Domain OrderItem")
    void testProtoToDomainOrderItem() {
      // Given
      com.mycompany.model.api.proto.OrderItem protoItem = com.mycompany.model.api.proto.OrderItem.newBuilder()
          .setId("item1")
          .setProductId("prod1")
          .setProductName("Product 1")
          .setQuantity(10)
          .setPrice(99.99)
          .build();

      // When
      OrderItem domainItem = mapper.protoToDomainOrderItem(protoItem);

      // Then
      assertNotNull(domainItem);
      assertEquals("item1", domainItem.getId());
      assertEquals("prod1", domainItem.getProductId());
      assertEquals("Product 1", domainItem.getProductName());
      assertEquals(10, domainItem.getQuantity());
      assertEquals(99.99, domainItem.getPrice().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Should convert Domain OrderItem to Proto OrderItem")
    void testDomainToProtoOrderItem() {
      // Given
      OrderItem domainItem = new OrderItem();
      domainItem.setId("item1");
      domainItem.setProductId("prod1");
      domainItem.setProductName("Product 1");
      domainItem.setQuantity(10);
      domainItem.setPrice(new BigDecimal("99.99"));

      // When
      com.mycompany.model.api.proto.OrderItem protoItem = mapper.domainToProtoOrderItem(domainItem);

      // Then
      assertNotNull(protoItem);
      assertEquals("item1", protoItem.getId());
      assertEquals("prod1", protoItem.getProductId());
      assertEquals("Product 1", protoItem.getProductName());
      assertEquals(10, protoItem.getQuantity());
      assertEquals(99.99, protoItem.getPrice(), 0.001);
    }
  }

  @Nested
  @DisplayName("List Mapping Tests")
  class ListMappingTests {

    @Test
    @DisplayName("Should convert list of Proto Orders to Domain Orders")
    void testProtoToDomainOrders() {
      // Given
      com.mycompany.model.api.proto.OrderItem protoItem = com.mycompany.model.api.proto.OrderItem.newBuilder()
          .setId("item1")
          .setProductId("prod1")
          .setProductName("Product 1")
          .setQuantity(10)
          .setPrice(99.99)
          .build();

      Date orderDate = Date.newBuilder()
          .setYear(2025)
          .setMonth(2)
          .setDay(26)
          .build();

      Timestamp lastUpdateTimestamp = Timestamp.newBuilder()
          .setSeconds(1616161616L)
          .setNanos(161000000)
          .build();

      com.mycompany.model.api.proto.Order protoOrder = com.mycompany.model.api.proto.Order.newBuilder()
          .setId("order1")
          .setSalesChannel("Online")
          .setStatus("COMPLETED")
          .setOrderDate(orderDate)
          .setLastUpdateTimestamp(lastUpdateTimestamp)
          .addItems(protoItem)
          .build();

      List<com.mycompany.model.api.proto.Order> protoOrders = Arrays.asList(protoOrder, protoOrder);

      // When
      List<Order> domainOrders = mapper.protoToDomainOrders(protoOrders);

      // Then
      assertNotNull(domainOrders);
      assertEquals(2, domainOrders.size());

      // Verify first item
      Order firstOrder = domainOrders.get(0);
      assertEquals("order1", firstOrder.getId());
      assertEquals(Order.StatusEnum.COMPLETED, firstOrder.getStatus());
    }

    @Test
    @DisplayName("Should convert list of Domain Orders to Proto Orders")
    void testDomainToProtoOrders() {
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
      OffsetDateTime updateTime = OffsetDateTime.ofInstant(
          Instant.ofEpochSecond(1616161616L, 161000000),
          ZoneOffset.UTC
      );
      domainOrder.setLastUpdateTimestamp(updateTime);

      domainOrder.setItems(Collections.singletonList(domainItem));

      List<Order> domainOrders = Arrays.asList(domainOrder, domainOrder);

      // When
      List<com.mycompany.model.api.proto.Order> protoOrders = mapper.domainToProtoOrders(domainOrders);

      // Then
      assertNotNull(protoOrders);
      assertEquals(2, protoOrders.size());

      // Verify first item
      com.mycompany.model.api.proto.Order firstOrder = protoOrders.get(0);
      assertEquals("order1", firstOrder.getId());
      assertEquals("COMPLETED", firstOrder.getStatus());
    }

    @Test
    @DisplayName("Should convert list of Proto OrderItems to Domain OrderItems")
    void testProtoToDomainOrderItems() {
      // Given
      com.mycompany.model.api.proto.OrderItem protoItem1 = com.mycompany.model.api.proto.OrderItem.newBuilder()
          .setId("item1")
          .setProductId("prod1")
          .setProductName("Product 1")
          .setQuantity(10)
          .setPrice(99.99)
          .build();

      com.mycompany.model.api.proto.OrderItem protoItem2 = com.mycompany.model.api.proto.OrderItem.newBuilder()
          .setId("item2")
          .setProductId("prod2")
          .setProductName("Product 2")
          .setQuantity(5)
          .setPrice(49.99)
          .build();

      List<com.mycompany.model.api.proto.OrderItem> protoItems = Arrays.asList(protoItem1, protoItem2);

      // When
      List<OrderItem> domainItems = mapper.protoToDomainOrderItems(protoItems);

      // Then
      assertNotNull(domainItems);
      assertEquals(2, domainItems.size());
      assertEquals("item1", domainItems.get(0).getId());
      assertEquals("item2", domainItems.get(1).getId());
    }

    @Test
    @DisplayName("Should convert list of Domain OrderItems to Proto OrderItems")
    void testDomainToProtoOrderItems() {
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
      List<com.mycompany.model.api.proto.OrderItem> protoItems = mapper.domainToProtoOrderItems(domainItems);

      // Then
      assertNotNull(protoItems);
      assertEquals(2, protoItems.size());
      assertEquals("item1", protoItems.get(0).getId());
      assertEquals("item2", protoItems.get(1).getId());
    }
  }

  @Nested
  @DisplayName("Custom Mapping Tests")
  class CustomMappingTests {

    @Test
    @DisplayName("Should map status values correctly")
    void testStatusMapping() {
      // Given & When & Then
      assertEquals(Order.StatusEnum.COMPLETED, mapper.mapStatus("COMPLETED"));
      assertEquals("COMPLETED", mapper.mapStatus(Order.StatusEnum.COMPLETED));

      // Handle null case
      assertNull(mapper.mapStatus((Order.StatusEnum) null));
    }

    @Test
    @DisplayName("Should map date values correctly")
    void testDateMapping() {
      // Given
      LocalDate localDate = LocalDate.of(2025, 2, 26);
      Date protoDate = Date.newBuilder()
          .setYear(2025)
          .setMonth(2)
          .setDay(26)
          .build();

      // When & Then
      assertEquals(localDate, mapper.dateToLocalDate(protoDate));

      Date mappedDate = mapper.localDateToDate(localDate);
      assertEquals(2025, mappedDate.getYear());
      assertEquals(2, mappedDate.getMonth());
      assertEquals(26, mappedDate.getDay());
    }

    @Test
    @DisplayName("Should map timestamp values correctly")
    void testTimestampMapping() {
      // Given
      Timestamp protoTimestamp = Timestamp.newBuilder()
          .setSeconds(1616161616L)
          .setNanos(161000000)
          .build();

      OffsetDateTime expectedDateTime = OffsetDateTime.ofInstant(
          Instant.ofEpochSecond(1616161616L, 161000000),
          ZoneOffset.UTC);

      // When & Then
      OffsetDateTime resultDateTime = mapper.timestampToOffsetDateTime(protoTimestamp);
      assertEquals(expectedDateTime.toInstant().getEpochSecond(), resultDateTime.toInstant().getEpochSecond());
      assertEquals(expectedDateTime.toInstant().getNano(), resultDateTime.toInstant().getNano());

      // Test reverse mapping
      Timestamp resultTimestamp = mapper.offsetDateTimeToTimestamp(expectedDateTime);
      assertEquals(1616161616L, resultTimestamp.getSeconds());
      assertEquals(161000000, resultTimestamp.getNanos());
    }

    @Test
    @DisplayName("Should handle null values correctly for all mappers")
    void testNullMappings() {
      // Test null LocalDate mappings
      assertNull(mapper.dateToLocalDate(null));
      assertNull(mapper.localDateToDate(null));

      // Test null timestamp mappings
      assertNull(mapper.timestampToOffsetDateTime(null));
      assertNull(mapper.offsetDateTimeToTimestamp(null));

      // Test null status mapping
      assertNull(mapper.mapStatus((String) null));
    }
  }
}
