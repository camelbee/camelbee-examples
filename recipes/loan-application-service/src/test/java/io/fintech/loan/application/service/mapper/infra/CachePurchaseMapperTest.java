package io.fintech.loan.application.service.mapper.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.model.domain.OrderItem;
import io.fintech.loan.application.service.model.infra.cache.Purchase;
import io.fintech.loan.application.service.model.infra.cache.PurchaseItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("CachePurchaseMapper Tests")
class CachePurchaseMapperTest {

  private final CachePurchaseMapper mapper = Mappers.getMapper(CachePurchaseMapper.class);

  @Test
  @DisplayName("Should map Cache Purchase to Domain Order")
  void cachePurchaseToDomainOrder_shouldMapAllFields() {
    String id = "123";
    String salesChannel = "ONLINE";
    String status = "CONFIRMED";
    LocalDate purchaseDate = LocalDate.of(2023, 1, 15);
    LocalDateTime updateTimestamp = LocalDateTime.now();

    Purchase purchase = new Purchase();
    purchase.setId(id);
    purchase.setSalesChannel(salesChannel);
    purchase.setStatus(status);
    purchase.setPurchaseDate(purchaseDate);
    purchase.setLastUpdateTimestamp(updateTimestamp);

    PurchaseItem item = new PurchaseItem();
    item.setId("item1");
    item.setProductId("prod1");
    item.setProductName("Test Product");
    item.setQuantity(2);
    item.setPrice(new BigDecimal("10.5"));

    purchase.setItems(List.of(item));

    Order domainOrder = mapper.cachePurchaseToDomainOrder(purchase);

    assertEquals(id, domainOrder.getId());
    assertEquals(salesChannel, domainOrder.getSalesChannel());
    assertEquals(Order.StatusEnum.valueOf(status), domainOrder.getStatus());
    assertEquals(purchaseDate, domainOrder.getOrderDate());
    assertEquals(updateTimestamp, domainOrder.getLastUpdateTimestamp().toLocalDateTime());
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
  @DisplayName("Should map Domain Order to Cache Purchase")
  void domainOrderToCachePurchase_shouldMapAllFields() {
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

    Purchase purchase = mapper.domainOrderToCachePurchase(domainOrder);

    assertEquals(id, purchase.getId());
    assertEquals(salesChannel, purchase.getSalesChannel());
    assertEquals(status.name(), purchase.getStatus());
    assertEquals(orderDate, purchase.getPurchaseDate());
    assertEquals(updateTimestamp.toLocalDateTime(), purchase.getLastUpdateTimestamp());
    assertNotNull(purchase.getItems());
    assertEquals(1, purchase.getItems().size());

    PurchaseItem purchaseItem = purchase.getItems().get(0);
    assertEquals("item1", purchaseItem.getId());
    assertEquals("prod1", purchaseItem.getProductId());
    assertEquals("Test Product", purchaseItem.getProductName());
    assertEquals(2, purchaseItem.getQuantity());
    assertEquals(new BigDecimal("10.5"), purchaseItem.getPrice());
  }

  @Test
  @DisplayName("Should use default values for null dates")
  void domainOrderToCachePurchase_shouldUseDefaultValues() {
    Order domainOrder = new Order();
    domainOrder.setId("123");

    Purchase purchase = mapper.domainOrderToCachePurchase(domainOrder);

    assertNotNull(purchase.getPurchaseDate());
    assertNotNull(purchase.getLastUpdateTimestamp());
  }

  @Test
  @DisplayName("Should map Cache PurchaseItem to Domain OrderItem")
  void cachePurchaseItemToDomainOrderItem_shouldMapAllFields() {
    PurchaseItem item = new PurchaseItem();
    item.setId("item1");
    item.setProductId("prod1");
    item.setProductName("Test Product");
    item.setQuantity(2);
    item.setPrice(new BigDecimal("10.5"));

    OrderItem domainItem = mapper.cachePurchaseItemToDomainOrderItem(item);

    assertEquals("item1", domainItem.getId());
    assertEquals("prod1", domainItem.getProductId());
    assertEquals("Test Product", domainItem.getProductName());
    assertEquals(2, domainItem.getQuantity());
    assertEquals(new BigDecimal("10.5"), domainItem.getPrice());
  }

  @Test
  @DisplayName("Should map Domain OrderItem to Cache PurchaseItem")
  void domainOrderItemToCachePurchaseItem_shouldMapAllFields() {
    OrderItem domainItem = new OrderItem();
    domainItem.setId("item1");
    domainItem.setProductId("prod1");
    domainItem.setProductName("Test Product");
    domainItem.setQuantity(2);
    domainItem.setPrice(new BigDecimal("10.5"));

    PurchaseItem item = mapper.domainOrderItemToCachePurchaseItem(domainItem);

    assertEquals("item1", item.getId());
    assertEquals("prod1", item.getProductId());
    assertEquals("Test Product", item.getProductName());
    assertEquals(2, item.getQuantity());
    assertEquals(new BigDecimal("10.5"), item.getPrice());
  }

  @Test
  @DisplayName("Should map list of purchases to list of orders")
  void cachePurchasesToDomainOrders_shouldMapAllOrders() {
    Purchase purchase1 = new Purchase();
    purchase1.setId("1");

    Purchase purchase2 = new Purchase();
    purchase2.setId("2");

    List<Purchase> purchases = Arrays.asList(purchase1, purchase2);

    List<Order> orders = mapper.cachePurchasesToDomainOrders(purchases);

    assertEquals(2, orders.size());
    assertEquals("1", orders.get(0).getId());
    assertEquals("2", orders.get(1).getId());
  }

  @Test
  @DisplayName("Should map list of orders to list of purchases")
  void domainOrdersToCachePurchases_shouldMapAllOrders() {
    Order order1 = new Order();
    order1.setId("1");

    Order order2 = new Order();
    order2.setId("2");

    List<Order> orders = Arrays.asList(order1, order2);

    List<Purchase> purchases = mapper.domainOrdersToCachePurchases(orders);

    assertEquals(2, purchases.size());
    assertEquals("1", purchases.get(0).getId());
    assertEquals("2", purchases.get(1).getId());
  }

  @Test
  @DisplayName("Should convert status correctly")
  void mapStatus_shouldConvertCorrectly() {
    String status = "CONFIRMED";
    Order.StatusEnum domainStatus = Order.StatusEnum.CONFIRMED;

    assertEquals(domainStatus, mapper.mapStatus(status));
    assertEquals(status, mapper.mapStatus(domainStatus));
  }

  @Test
  @DisplayName("Should handle null status")
  void mapStatus_shouldHandleNull() {
    assertNull(mapper.mapStatus((Order.StatusEnum) null));
  }

  @Test
  @DisplayName("Should convert LocalDateTime and OffsetDateTime correctly")
  void mapLocalDateTimeAndOffsetDateTime_shouldConvertCorrectly() {
    LocalDateTime localDateTime = LocalDateTime.now();
    OffsetDateTime offsetDateTime = localDateTime.atOffset(ZoneOffset.UTC);

    assertEquals(offsetDateTime, mapper.map(localDateTime));
    assertEquals(localDateTime, mapper.map(offsetDateTime));
  }

  @Test
  @DisplayName("Should handle null date conversions")
  void mapNullLocalDateTimeAndOffsetDateTime_shouldReturnNull() {
    assertNull(mapper.map((LocalDateTime) null));
    assertNull(mapper.map((OffsetDateTime) null));
  }
}