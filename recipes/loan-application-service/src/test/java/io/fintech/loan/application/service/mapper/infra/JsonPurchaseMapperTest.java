package io.fintech.loan.application.service.mapper.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.model.domain.OrderItem;
import io.fintech.loan.application.service.model.infra.json.Purchase;
import io.fintech.loan.application.service.model.infra.json.Purchase.StatusEnum;
import io.fintech.loan.application.service.model.infra.json.PurchaseItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class JsonPurchaseMapperTest {

  private final JsonPurchaseMapper mapper = Mappers.getMapper(JsonPurchaseMapper.class);

  @Test
  void jsonPurchaseToDomainOrder_shouldMapAllFields() {
    // Given
    String id = "123";
    String salesChannel = "ONLINE";
    String status = "CONFIRMED";
    LocalDate purchaseDate = LocalDate.of(2023, 1, 15);
    OffsetDateTime updateTimestamp = OffsetDateTime.now();

    Purchase jsonPurchase = new Purchase();
    jsonPurchase.setId(id);
    jsonPurchase.setSalesChannel(salesChannel);
    jsonPurchase.setStatus(StatusEnum.CONFIRMED);
    jsonPurchase.setPurchaseDate(purchaseDate);
    jsonPurchase.setLastUpdateTimestamp(updateTimestamp);

    PurchaseItem jsonItem = new PurchaseItem();
    jsonItem.setId("item1");
    jsonItem.setProductId("prod1");
    jsonItem.setProductName("Test Product");
    jsonItem.setQuantity(2);
    jsonItem.setPrice(new BigDecimal("10.5"));

    jsonPurchase.setItems(List.of(jsonItem));

    // When
    Order domainOrder = mapper.jsonPurchaseToDomainOrder(jsonPurchase);

    // Then
    assertEquals(id, domainOrder.getId());
    assertEquals(salesChannel, domainOrder.getSalesChannel());
    assertEquals(Order.StatusEnum.valueOf(status), domainOrder.getStatus());
    assertEquals(purchaseDate, domainOrder.getOrderDate());
    assertEquals(updateTimestamp, domainOrder.getLastUpdateTimestamp());
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
  void domainOrderToJsonPurchase_shouldMapAllFields() {
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

    domainOrder.setItems(List.of(domainItem));

    // When
    Purchase jsonPurchase = mapper.domainOrderToJsonPurchase(domainOrder);

    // Then
    assertEquals(id, jsonPurchase.getId());
    assertEquals(salesChannel, jsonPurchase.getSalesChannel());
    assertEquals(StatusEnum.CONFIRMED, jsonPurchase.getStatus());
    assertEquals(orderDate, jsonPurchase.getPurchaseDate());
    // Use isEqual to compare the actual instants regardless of zone representation
    assertTrue(updateTimestamp.isEqual(jsonPurchase.getLastUpdateTimestamp()),
        "Timestamps should represent the same instant regardless of zone");
    assertNotNull(jsonPurchase.getItems());
    assertEquals(1, jsonPurchase.getItems().size());

    PurchaseItem jsonItem = jsonPurchase.getItems().get(0);
    assertEquals("item1", jsonItem.getId());
    assertEquals("prod1", jsonItem.getProductId());
    assertEquals("Test Product", jsonItem.getProductName());
    assertEquals(2, jsonItem.getQuantity());
    assertEquals(new BigDecimal("10.5"), jsonItem.getPrice());
  }

  @Test
  void jsonPurchaseItemToDomainOrderItem_shouldMapAllFields() {
    // Given
    PurchaseItem jsonItem = new PurchaseItem();
    jsonItem.setId("item1");
    jsonItem.setProductId("prod1");
    jsonItem.setProductName("Test Product");
    jsonItem.setQuantity(2);
    jsonItem.setPrice(new BigDecimal("10.5"));

    // When
    OrderItem domainItem = mapper.jsonPurchaseItemToDomainOrderItem(jsonItem);

    // Then
    assertEquals("item1", domainItem.getId());
    assertEquals("prod1", domainItem.getProductId());
    assertEquals("Test Product", domainItem.getProductName());
    assertEquals(2, domainItem.getQuantity());
    assertEquals(new BigDecimal("10.5"), domainItem.getPrice());
  }

  @Test
  void domainOrderItemToJsonPurchaseItem_shouldMapAllFields() {
    // Given
    OrderItem domainItem = new OrderItem();
    domainItem.setId("item1");
    domainItem.setProductId("prod1");
    domainItem.setProductName("Test Product");
    domainItem.setQuantity(2);
    domainItem.setPrice(new BigDecimal("10.5"));

    // When
    PurchaseItem jsonItem = mapper.domainOrderItemToJsonPurchaseItem(domainItem);

    // Then
    assertEquals("item1", jsonItem.getId());
    assertEquals("prod1", jsonItem.getProductId());
    assertEquals("Test Product", jsonItem.getProductName());
    assertEquals(2, jsonItem.getQuantity());
    assertEquals(new BigDecimal("10.5"), jsonItem.getPrice());
  }

  @Test
  void jsonPurchasesToDomainOrders_shouldMapAllOrders() {
    // Given
    Purchase purchase1 = new Purchase();
    purchase1.setId("1");

    Purchase purchase2 = new Purchase();
    purchase2.setId("2");

    List<Purchase> purchases = Arrays.asList(purchase1, purchase2);

    // When
    List<Order> orders = mapper.jsonPurchasesToDomainOrders(purchases);

    // Then
    assertEquals(2, orders.size());
    assertEquals("1", orders.get(0).getId());
    assertEquals("2", orders.get(1).getId());
  }

  @Test
  void domainOrdersToJsonPurchases_shouldMapAllOrders() {
    // Given
    Order order1 = new Order();
    order1.setId("1");

    Order order2 = new Order();
    order2.setId("2");

    List<Order> orders = Arrays.asList(order1, order2);

    // When
    List<Purchase> purchases = mapper.domainOrdersToJsonPurchases(orders);

    // Then
    assertEquals(2, purchases.size());
    assertEquals("1", purchases.get(0).getId());
    assertEquals("2", purchases.get(1).getId());
  }

  @Test
  void mapLocalDateTimeAndOffsetDateTime_shouldConvertCorrectly() {
    // Given
    LocalDateTime localDateTime = LocalDateTime.now();
    OffsetDateTime offsetDateTime = localDateTime.atOffset(ZoneOffset.UTC);

    // When & Then
    assertEquals(offsetDateTime, mapper.map(localDateTime));
    assertEquals(localDateTime, mapper.map(offsetDateTime));
  }

  @Test
  void mapNullLocalDateTimeAndOffsetDateTime_shouldReturnNull() {
    // When & Then
    assertNull(mapper.map((LocalDateTime) null));
    assertNull(mapper.map((OffsetDateTime) null));
  }
}