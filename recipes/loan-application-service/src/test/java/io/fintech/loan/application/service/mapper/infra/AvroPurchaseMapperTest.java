package io.fintech.loan.application.service.mapper.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.model.domain.OrderItem;
import io.fintech.loan.application.service.model.infra.avro.Purchase;
import io.fintech.loan.application.service.model.infra.avro.PurchaseItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AvroPurchaseMapperTest {

  private final AvroPurchaseMapper mapper = Mappers.getMapper(AvroPurchaseMapper.class);

  @Test
  void avroPurchaseToDomainOrder_shouldMapAllFields() {
    // Given
    String id = "123";
    String salesChannel = "ONLINE";
    CharSequence status = "CONFIRMED";
    int purchaseDate = (int) LocalDate.of(2023, 1, 15).toEpochDay();
    Long updateTimestamp = Instant.now().toEpochMilli();

    Purchase avroPurchase = new Purchase();
    avroPurchase.setId(id);
    avroPurchase.setSalesChannel(salesChannel);
    avroPurchase.setStatus(status);
    avroPurchase.setPurchaseDate(purchaseDate);
    avroPurchase.setLastUpdateTimestamp(updateTimestamp);

    PurchaseItem avroItem = new PurchaseItem();
    avroItem.setId("item1");
    avroItem.setProductId("prod1");
    avroItem.setProductName("Test Product");
    avroItem.setQuantity(2);
    avroItem.setPrice(10.5);

    avroPurchase.setItems(Arrays.asList(avroItem));

    // When
    Order domainOrder = mapper.avroPurchaseToDomainOrder(avroPurchase);

    // Then
    assertEquals(id, domainOrder.getId());
    assertEquals(salesChannel, domainOrder.getSalesChannel());
    assertEquals(Order.StatusEnum.fromValue(status.toString()), domainOrder.getStatus());
    assertEquals(LocalDate.ofEpochDay(purchaseDate), domainOrder.getOrderDate());
    // Check if the timestamps match when converted to the same format
    assertEquals(Instant.ofEpochMilli(updateTimestamp), domainOrder.getLastUpdateTimestamp().toInstant());
    assertNotNull(domainOrder.getItems());
    assertEquals(1, domainOrder.getItems().size());

    OrderItem domainItem = domainOrder.getItems().get(0);
    assertEquals("item1", domainItem.getId());
    assertEquals("prod1", domainItem.getProductId());
    assertEquals("Test Product", domainItem.getProductName());
    assertEquals(2, domainItem.getQuantity());
    assertEquals(new BigDecimal("10.5"), domainItem.getPrice());
  }

  @Test
  void domainOrderToAvroPurchase_shouldMapAllFields() {
    // Given
    String id = "123";
    String salesChannel = "ONLINE";
    Order.StatusEnum status = Order.StatusEnum.CONFIRMED;
    LocalDate orderDate = LocalDate.of(2023, 1, 15);
    OffsetDateTime updateTimestamp = OffsetDateTime.now();

    Order domainOrder = new Order();
    domainOrder.setId(id);
    domainOrder.setSalesChannel(salesChannel);
    domainOrder.setStatus(status);
    domainOrder.setOrderDate(orderDate);
    domainOrder.setLastUpdateTimestamp(updateTimestamp);

    OrderItem domainItem = new OrderItem();
    domainItem.setId("item1");
    domainItem.setProductId("prod1");
    domainItem.setProductName("Test Product");
    domainItem.setQuantity(2);
    domainItem.setPrice(new BigDecimal("10.5"));

    domainOrder.setItems(Arrays.asList(domainItem));

    // When
    Purchase avroPurchase = mapper.domainOrderToAvroPurchase(domainOrder);

    // Then
    assertEquals(id, avroPurchase.getId().toString());
    assertEquals(salesChannel, avroPurchase.getSalesChannel().toString());
    assertEquals(status.name(), avroPurchase.getStatus().toString());
    assertEquals((int) orderDate.toEpochDay(), avroPurchase.getPurchaseDate());
    // Check if the timestamp is correctly converted to epoch milliseconds
    assertEquals(updateTimestamp.toInstant().toEpochMilli(), avroPurchase.getLastUpdateTimestamp());
    assertNotNull(avroPurchase.getItems());
    assertEquals(1, avroPurchase.getItems().size());

    PurchaseItem avroItem = avroPurchase.getItems().get(0);
    assertEquals("item1", avroItem.getId().toString());
    assertEquals("prod1", avroItem.getProductId().toString());
    assertEquals("Test Product", avroItem.getProductName().toString());
    assertEquals(2, avroItem.getQuantity());
    assertEquals(10.5, avroItem.getPrice());
  }

  @Test
  void avroPurchaseItemToDomainOrderItem_shouldMapAllFields() {
    // Given
    PurchaseItem avroItem = new PurchaseItem();
    avroItem.setId("item1");
    avroItem.setProductId("prod1");
    avroItem.setProductName("Test Product");
    avroItem.setQuantity(2);
    avroItem.setPrice(10.5);

    // When
    OrderItem domainItem = mapper.avroPurchaseItemToDomainOrderItem(avroItem);

    // Then
    assertEquals("item1", domainItem.getId());
    assertEquals("prod1", domainItem.getProductId());
    assertEquals("Test Product", domainItem.getProductName());
    assertEquals(2, domainItem.getQuantity());
    assertEquals(new BigDecimal("10.5"), domainItem.getPrice());
  }

  @Test
  void domainOrderItemToAvroPurchaseItem_shouldMapAllFields() {
    // Given
    OrderItem domainItem = new OrderItem();
    domainItem.setId("item1");
    domainItem.setProductId("prod1");
    domainItem.setProductName("Test Product");
    domainItem.setQuantity(2);
    domainItem.setPrice(new BigDecimal("10.5"));

    // When
    PurchaseItem avroItem = mapper.domainOrderItemToAvroPurchaseItem(domainItem);

    // Then
    assertEquals("item1", avroItem.getId().toString());
    assertEquals("prod1", avroItem.getProductId().toString());
    assertEquals("Test Product", avroItem.getProductName().toString());
    assertEquals(2, avroItem.getQuantity());
    assertEquals(10.5, avroItem.getPrice());
  }

  @Test
  void domainOrdersToAvroPurchases_shouldMapAllOrders() {
    // Given
    Order order1 = new Order();
    order1.setId("1");

    Order order2 = new Order();
    order2.setId("2");

    List<Order> orders = Arrays.asList(order1, order2);

    // When
    List<Purchase> avroPurchases = mapper.domainOrdersToAvroPurchases(orders);

    // Then
    assertEquals(2, avroPurchases.size());
    assertEquals("1", avroPurchases.get(0).getId().toString());
    assertEquals("2", avroPurchases.get(1).getId().toString());
  }

  @Test
  void mapLongAndOffsetDateTime_shouldCorrectlyConvert() {
    // Given
    Long epochMillis = Instant.now().toEpochMilli();
    OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());

    // When & Then
    assertEquals(epochMillis, mapper.mapInstant(offsetDateTime.toInstant()));
    assertEquals(offsetDateTime.toInstant(), Instant.ofEpochMilli(mapper.mapInstant(offsetDateTime.toInstant())));
  }

  @Test
  void mapNullLongAndOffsetDateTime_shouldReturnNull() {
    // When & Then
    assertNull(mapper.mapInstant((Instant) null));
    assertNull(mapper.map((OffsetDateTime) null));
  }

  @Test
  void mapLocalDateToInteger_shouldConvertCorrectly() {
    // Given
    LocalDate date = LocalDate.of(2023, 1, 15);
    int epochDay = (int) date.toEpochDay();

    // When & Then
    assertEquals(date, mapper.map(epochDay));
    assertEquals(epochDay, mapper.map(date));
  }

  @Test
  void mapNullLocalDateAndInteger_shouldReturnNull() {
    // When & Then
    assertNull(mapper.map((Integer) null));
    assertNull(mapper.map((LocalDate) null));
  }
}
