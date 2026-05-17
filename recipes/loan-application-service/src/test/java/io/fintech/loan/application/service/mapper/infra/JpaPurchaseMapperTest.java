package io.fintech.loan.application.service.mapper.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.model.domain.OrderItem;
import io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase;
import io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase.StatusEnum;
import io.fintech.loan.application.service.model.infra.jpa.postgresql.PurchaseItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class JpaPurchaseMapperTest {

  private final JpaPurchaseMapper mapper = Mappers.getMapper(JpaPurchaseMapper.class);

  @Test
  void jpaPurchaseToDomainOrder_shouldMapAllFields() {
    // Given
    Long id = 123L;
    String salesChannel = "ONLINE";
    String status = "CONFIRMED";
    LocalDate purchaseDate = LocalDate.of(2023, 1, 15);
    LocalDateTime updateTimestamp = LocalDateTime.now();

    Purchase jpaPurchase = new Purchase();
    jpaPurchase.setId(id);
    jpaPurchase.setSalesChannel(salesChannel);
    jpaPurchase.setStatus(StatusEnum.CONFIRMED);
    jpaPurchase.setPurchaseDate(purchaseDate);
    jpaPurchase.setLastUpdateTimestamp(updateTimestamp);

    PurchaseItem jpaItem = new PurchaseItem();
    jpaItem.setId(1L);
    jpaItem.setProductId("prod1");
    jpaItem.setProductName("Test Product");
    jpaItem.setQuantity(2);
    jpaItem.setPrice(new BigDecimal("10.5"));

    jpaPurchase.setItems(Arrays.asList(jpaItem));

    // When
    Order domainOrder = mapper.jpaPurchaseToDomainOrder(jpaPurchase);

    // Then
    assertEquals(id.toString(), domainOrder.getId());
    assertEquals(salesChannel, domainOrder.getSalesChannel());
    assertEquals(Order.StatusEnum.valueOf(status), domainOrder.getStatus());
    assertEquals(purchaseDate, domainOrder.getOrderDate());
    assertEquals(updateTimestamp, domainOrder.getLastUpdateTimestamp().toLocalDateTime());
    assertNotNull(domainOrder.getItems());
    assertEquals(1, domainOrder.getItems().size());

    OrderItem domainItem = domainOrder.getItems().get(0);
    assertEquals("1", domainItem.getId());
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
    domainItem.setId("1");
    domainItem.setProductId("prod1");
    domainItem.setProductName("Test Product");
    domainItem.setQuantity(2);
    domainItem.setPrice(new BigDecimal("10.5"));

    domainOrder.setItems(List.of(domainItem));

    // When
    Purchase jpaPurchase = mapper.domainOrderToJsonPurchase(domainOrder);

    // Then
    assertEquals(123L, jpaPurchase.getId());
    assertEquals(salesChannel, jpaPurchase.getSalesChannel());
    assertEquals(StatusEnum.CONFIRMED, jpaPurchase.getStatus());
    assertEquals(orderDate, jpaPurchase.getPurchaseDate());
    assertEquals(updateTimestamp.toLocalDateTime(), jpaPurchase.getLastUpdateTimestamp());
    assertNotNull(jpaPurchase.getItems());
    assertEquals(1, jpaPurchase.getItems().size());

    PurchaseItem jpaItem = jpaPurchase.getItems().get(0);
    assertEquals(1L, jpaItem.getId());
    assertEquals("prod1", jpaItem.getProductId());
    assertEquals("Test Product", jpaItem.getProductName());
    assertEquals(2, jpaItem.getQuantity());
    assertEquals(new BigDecimal("10.5"), jpaItem.getPrice());
  }

  @Test
  void domainOrderToJsonPurchase_shouldUseDefaultValues() {
    // Given
    Order domainOrder = new Order();
    domainOrder.setId("123");

    // OrderDate and LastUpdateTimestamp are null

    // When
    Purchase jpaPurchase = mapper.domainOrderToJsonPurchase(domainOrder);

    // Then
    assertNotNull(jpaPurchase.getPurchaseDate());  // Default to current date
    assertNotNull(jpaPurchase.getLastUpdateTimestamp());  // Default to current timestamp
  }

  @Test
  void jpaPurchaseItemToDomainOrderItem_shouldMapAllFields() {
    // Given
    PurchaseItem jpaItem = new PurchaseItem();
    jpaItem.setId(1L);
    jpaItem.setProductId("prod1");
    jpaItem.setProductName("Test Product");
    jpaItem.setQuantity(2);
    jpaItem.setPrice(new BigDecimal("10.5"));

    // When
    OrderItem domainItem = mapper.jpaPurchaseItemToDomainOrderItem(jpaItem);

    // Then
    assertEquals("1", domainItem.getId());
    assertEquals("prod1", domainItem.getProductId());
    assertEquals("Test Product", domainItem.getProductName());
    assertEquals(2, domainItem.getQuantity());
    assertEquals(new BigDecimal("10.5"), domainItem.getPrice());
  }

  @Test
  void domainOrderItemToJsonPurchaseItem_shouldMapAllFields() {
    // Given
    OrderItem domainItem = new OrderItem();
    domainItem.setId("1");
    domainItem.setProductId("prod1");
    domainItem.setProductName("Test Product");
    domainItem.setQuantity(2);
    domainItem.setPrice(new BigDecimal("10.5"));

    // When
    PurchaseItem jpaItem = mapper.domainOrderItemToJsonPurchaseItem(domainItem);

    // Then
    assertEquals(1L, jpaItem.getId());
    assertEquals("prod1", jpaItem.getProductId());
    assertEquals("Test Product", jpaItem.getProductName());
    assertEquals(2, jpaItem.getQuantity());
    assertEquals(new BigDecimal("10.5"), jpaItem.getPrice());
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

  @Test
  void safeStringToInteger_shouldConvertValidString() {
    // Given
    String validNumber = "123";

    // When
    Long result = mapper.safeStringToInteger(validNumber);

    // Then
    assertEquals(123L, result);
  }

  @Test
  void safeStringToInteger_shouldHandleInvalidString() {
    // Given
    String invalidNumber = "abc";

    // When
    Long result = mapper.safeStringToInteger(invalidNumber);

    // Then
    assertNull(result);
  }

  @Test
  void safeStringToInteger_shouldHandleNullString() {
    // When
    Long result = mapper.safeStringToInteger(null);

    // Then
    assertNull(result);
  }
}
