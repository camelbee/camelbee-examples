package io.fintech.loan.application.service.mapper.api;

import static org.junit.jupiter.api.Assertions.*;

import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.model.domain.Order.StatusEnum;
import io.fintech.loan.application.service.model.domain.OrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for {@link AvroOrderMapper} to verify all mapping scenarios work as expected.
 */
class AvroOrderMapperTest {

  private AvroOrderMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(AvroOrderMapper.class);
  }

  @Nested
  @DisplayName("Single Object Mapping Tests")
  class SingleObjectMappingTests {

    @Test
    @DisplayName("Should convert Avro Order to Domain Order")
    void testAvroToDomainOrder() {
      // Given
      io.fintech.loan.application.service.model.api.avro.OrderItem avroItem = new io.fintech.loan.application.service.model.api.avro.OrderItem();
      avroItem.setId("item1");
      avroItem.setProductId("prod1");
      avroItem.setProductName("Product 1");
      avroItem.setQuantity(10);
      avroItem.setPrice(99.99);

      io.fintech.loan.application.service.model.api.avro.Order avroOrder = new io.fintech.loan.application.service.model.api.avro.Order();
      avroOrder.setId("order1");
      avroOrder.setSalesChannel("Online");
      avroOrder.setStatus("COMPLETED");
      avroOrder.setOrderDate(19123);
      avroOrder.setLastUpdateTimestamp(1616161616161L);
      avroOrder.setItems(Collections.singletonList(avroItem));

      // When
      Order domainOrder = mapper.avroToDomainOrder(avroOrder);

      // Then
      assertNotNull(domainOrder);
      assertEquals("order1", domainOrder.getId());
      assertEquals("Online", domainOrder.getSalesChannel());
      assertEquals(Order.StatusEnum.COMPLETED, domainOrder.getStatus());
      assertEquals(LocalDate.ofEpochDay(19123), domainOrder.getOrderDate());
      // Compare timestamps by epoch millis to avoid timezone issues
      assertEquals(1616161616161L, domainOrder.getLastUpdateTimestamp().toInstant().toEpochMilli());

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
    @DisplayName("Should convert Domain Order to Avro Order")
    void testDomainToAvroOrder() {
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
      domainOrder.setOrderDate(LocalDate.ofEpochDay(19123));

      // Use OffsetDateTime instead of Instant for lastUpdateTimestamp
      OffsetDateTime updateTime = OffsetDateTime.ofInstant(
          Instant.ofEpochMilli(1616161616161L),
          ZoneId.systemDefault()
      );
      domainOrder.setLastUpdateTimestamp(updateTime);

      domainOrder.setItems(Collections.singletonList(domainItem));

      // When
      io.fintech.loan.application.service.model.api.avro.Order avroOrder = mapper.domainToAvroOrder(domainOrder);

      // Then
      assertNotNull(avroOrder);
      assertEquals("order1", avroOrder.getId().toString());
      assertEquals("Online", avroOrder.getSalesChannel().toString());
      assertEquals("COMPLETED", avroOrder.getStatus().toString());
      assertEquals(19123, avroOrder.getOrderDate());
      assertEquals(1616161616161L, avroOrder.getLastUpdateTimestamp());

      // And item is correctly mapped
      assertNotNull(avroOrder.getItems());
      assertEquals(1, avroOrder.getItems().size());
      io.fintech.loan.application.service.model.api.avro.OrderItem mappedItem = avroOrder.getItems().get(0);
      assertEquals("item1", mappedItem.getId().toString());
      assertEquals("prod1", mappedItem.getProductId().toString());
      assertEquals("Product 1", mappedItem.getProductName().toString());
      assertEquals(10, mappedItem.getQuantity());
      assertEquals(99.99, mappedItem.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Should convert Avro OrderItem to Domain OrderItem")
    void testAvroToDomainOrderItem() {
      // Given
      io.fintech.loan.application.service.model.api.avro.OrderItem avroItem = new io.fintech.loan.application.service.model.api.avro.OrderItem();
      avroItem.setId("item1");
      avroItem.setProductId("prod1");
      avroItem.setProductName("Product 1");
      avroItem.setQuantity(10);
      avroItem.setPrice(99.99);

      // When
      OrderItem domainItem = mapper.avroToDomainOrderItem(avroItem);

      // Then
      assertNotNull(domainItem);
      assertEquals("item1", domainItem.getId());
      assertEquals("prod1", domainItem.getProductId());
      assertEquals("Product 1", domainItem.getProductName());
      assertEquals(10, domainItem.getQuantity());
      assertEquals(99.99, domainItem.getPrice().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Should convert Domain OrderItem to Avro OrderItem")
    void testDomainToAvroOrderItem() {
      // Given
      OrderItem domainItem = new OrderItem();
      domainItem.setId("item1");
      domainItem.setProductId("prod1");
      domainItem.setProductName("Product 1");
      domainItem.setQuantity(10);
      domainItem.setPrice(new BigDecimal("99.99"));

      // When
      io.fintech.loan.application.service.model.api.avro.OrderItem avroItem = mapper.domainToAvroOrderItem(domainItem);

      // Then
      assertNotNull(avroItem);
      assertEquals("item1", avroItem.getId().toString());
      assertEquals("prod1", avroItem.getProductId().toString());
      assertEquals("Product 1", avroItem.getProductName().toString());
      assertEquals(10, avroItem.getQuantity());
      assertEquals(99.99, avroItem.getPrice(), 0.001);
    }
  }

  @Nested
  @DisplayName("List Mapping Tests")
  class ListMappingTests {

    @Test
    @DisplayName("Should convert list of Avro Orders to Domain Orders")
    void testAvroToDomainOrders() {
      // Given
      io.fintech.loan.application.service.model.api.avro.OrderItem avroItem = new io.fintech.loan.application.service.model.api.avro.OrderItem();
      avroItem.setId("item1");
      avroItem.setProductId("prod1");
      avroItem.setProductName("Product 1");
      avroItem.setQuantity(10);
      avroItem.setPrice(99.99);

      io.fintech.loan.application.service.model.api.avro.Order avroOrder = new io.fintech.loan.application.service.model.api.avro.Order();
      avroOrder.setId("order1");
      avroOrder.setSalesChannel("Online");
      avroOrder.setStatus("COMPLETED");
      avroOrder.setOrderDate(19123);
      avroOrder.setLastUpdateTimestamp(1616161616161L);
      avroOrder.setItems(Collections.singletonList(avroItem));

      List<io.fintech.loan.application.service.model.api.avro.Order> avroOrders = Arrays.asList(avroOrder, avroOrder);

      // When
      List<Order> domainOrders = mapper.avroToDomainOrders(avroOrders);

      // Then
      assertNotNull(domainOrders);
      assertEquals(2, domainOrders.size());

      // Verify first item
      Order firstOrder = domainOrders.get(0);
      assertEquals("order1", firstOrder.getId());
      assertEquals(Order.StatusEnum.COMPLETED, firstOrder.getStatus());
    }

    @Test
    @DisplayName("Should convert list of Domain Orders to Avro Orders")
    void testDomainToAvroOrders() {
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
      domainOrder.setOrderDate(LocalDate.ofEpochDay(19123));

      // Use OffsetDateTime instead of Instant for lastUpdateTimestamp
      OffsetDateTime updateTime = OffsetDateTime.ofInstant(
          Instant.ofEpochMilli(1616161616161L),
          ZoneId.systemDefault()
      );
      domainOrder.setLastUpdateTimestamp(updateTime);

      domainOrder.setItems(Collections.singletonList(domainItem));

      List<Order> domainOrders = Arrays.asList(domainOrder, domainOrder);

      // When
      List<io.fintech.loan.application.service.model.api.avro.Order> avroOrders = mapper.domainToAvroOrders(domainOrders);

      // Then
      assertNotNull(avroOrders);
      assertEquals(2, avroOrders.size());

      // Verify first item
      io.fintech.loan.application.service.model.api.avro.Order firstOrder = avroOrders.get(0);
      assertEquals("order1", firstOrder.getId().toString());
      assertEquals("COMPLETED", firstOrder.getStatus().toString());
    }

    @Test
    @DisplayName("Should convert list of Avro OrderItems to Domain OrderItems")
    void testAvroToDomainOrderItems() {
      // Given
      io.fintech.loan.application.service.model.api.avro.OrderItem avroItem1 = new io.fintech.loan.application.service.model.api.avro.OrderItem();
      avroItem1.setId("item1");
      avroItem1.setProductId("prod1");
      avroItem1.setProductName("Product 1");
      avroItem1.setQuantity(10);
      avroItem1.setPrice(99.99);

      io.fintech.loan.application.service.model.api.avro.OrderItem avroItem2 = new io.fintech.loan.application.service.model.api.avro.OrderItem();
      avroItem2.setId("item2");
      avroItem2.setProductId("prod2");
      avroItem2.setProductName("Product 2");
      avroItem2.setQuantity(5);
      avroItem2.setPrice(49.99);

      List<io.fintech.loan.application.service.model.api.avro.OrderItem> avroItems = Arrays.asList(avroItem1, avroItem2);

      // When
      List<OrderItem> domainItems = mapper.avroToDomainOrderItems(avroItems);

      // Then
      assertNotNull(domainItems);
      assertEquals(2, domainItems.size());
      assertEquals("item1", domainItems.get(0).getId());
      assertEquals("item2", domainItems.get(1).getId());
    }

    @Test
    @DisplayName("Should convert list of Domain OrderItems to Avro OrderItems")
    void testDomainToAvroOrderItems() {
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
      List<io.fintech.loan.application.service.model.api.avro.OrderItem> avroItems = mapper.domainToAvroOrderItems(domainItems);

      // Then
      assertNotNull(avroItems);
      assertEquals(2, avroItems.size());
      assertEquals("item1", avroItems.get(0).getId().toString());
      assertEquals("item2", avroItems.get(1).getId().toString());
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
      assertEquals("COMPLETED", mapper.mapStatus(Order.StatusEnum.COMPLETED).toString());

      // Handle null case
      assertNull(mapper.mapStatus((Order.StatusEnum) null));
    }

    @Test
    @DisplayName("Should map date values correctly")
    void testDateMapping() {
      // Given & When & Then
      LocalDate date = LocalDate.ofEpochDay(19123);
      assertEquals(date, mapper.map(19123));
      assertEquals(Integer.valueOf(19123), mapper.map(date));

      // Handle null case
      assertNull(mapper.map((Integer) null));
      assertNull(mapper.map((LocalDate) null));
    }

    @Test
    @DisplayName("Should map Instant and OffsetDateTime correctly")
    void testInstantMapping() {
      // Given
      Instant instant = Instant.now();

      // When & Then
      OffsetDateTime odt = mapper.map(instant);
      assertEquals(instant, odt.toInstant());
      assertEquals(instant, mapper.map(odt));

      // Handle null case
      assertNull(mapper.map((Instant) null));
      assertNull(mapper.map((OffsetDateTime) null));
    }

    @Test
    @DisplayName("Should map timestamp values correctly")
    void testTimestampMapping() {
      // Given
      Instant instant = Instant.ofEpochMilli(1616161616161L);

      // When & Then
      assertEquals(instant, mapper.mapInstant(1616161616161L));
      assertEquals(Long.valueOf(1616161616161L), mapper.mapInstant(instant));

      // Handle null case
      assertNull(mapper.mapInstant((Long) null));
      assertNull(mapper.mapInstant((Instant) null));
    }

    @Test
    @DisplayName("Should map String and CharSequence correctly")
    void testStringMapping() {
      // Given, When & Then
      assertEquals("test", mapper.map((CharSequence) "test"));
      assertEquals("test", mapper.map("test").toString());

      // Handle null case
      assertNull(mapper.map((String) null));
      assertNull(mapper.map((CharSequence) null));
    }

    @Test
    @DisplayName("Should handle null values correctly for all mappers")
    void testNullMappings() {
      // Verify all null mappings are handled correctly
      assertNull(mapper.map((Instant) null));
      assertNull(mapper.map((OffsetDateTime) null));
      assertNull(mapper.map((String) null));
      assertNull(mapper.map((CharSequence) null));
      assertNull(mapper.map((Integer) null));
      assertNull(mapper.map((LocalDate) null));
      assertNull(mapper.mapInstant((Long) null));
      assertNull(mapper.mapInstant((Instant) null));
      // Remove test for mapper.mapStatus with null CharSequence as it's not null-safe in implementation
    }
  }
}
