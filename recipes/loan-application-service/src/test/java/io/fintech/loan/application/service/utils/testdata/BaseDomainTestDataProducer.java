package io.fintech.loan.application.service.utils.testdata;

import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.model.domain.Order.StatusEnum;
import io.fintech.loan.application.service.model.domain.OrderItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;

/**
 * Base class for domain test data producers.
 * Contains common functionality shared across all test data producers.
 */
public abstract class BaseDomainTestDataProducer {

  protected static final String ONLINE_CHANNEL = "ONLINE";
  protected static final String WHOLESALE = "WHOLESALE";

  public static final String TXID_FOR_SUCCESS = "779d2950-7f02-440a-a4a3-b36b82ec6c29";
  public static final String TXID_FOR_REST_400_ERROR = "9d47cfe5-4bf3-41a3-abcf-9249e6b3fae4";
  public static final String TXID_FOR_REST_404_ERROR = "6911185b-f4f0-4435-b3e2-0720fc148b63";
  public static final String TXID_FOR_REST_500_ERROR = "6037ac1b-edae-4f82-8a8f-e7724041ae08";
  public static final String TXID_FOR_SOAP_CLIENT_ERROR = "b7945002-b869-499d-a83a-b98943492fba";
  public static final String TXID_FOR_SOAP_SERVER_ERROR = "0ac73dc6-12bd-44b5-9d62-1f5f1946e97a";
  public static final String TXID_FOR_GRPC_INVALIDARGUMENT_ERROR = "0491b5b2-fe13-456d-9b24-c3f64559910a";
  public static final String TXID_FOR_GRPC_NOTFOUND_ERROR = "55f47844-7038-4d22-8bd0-d270c1c0241f";
  public static final String TXID_FOR_GRPC_INTERNAL_ERROR = "e9551486-1abf-416c-8c58-f3b83ec688f2";

  /**
   * Creates a list of order items with specified parameters.
   */
  protected static List<OrderItem> generateOrderItems(int orderId, boolean withId, int numberOfItems,
      String productId, String productName, int productNameRepeat) {
    if (numberOfItems == 0) {
      return null;
    }
    List<OrderItem> items = new ArrayList<>();
    for (int i = 0; i < numberOfItems; i++) {
      // Calculate idSuffix and pad with leading "0" if less than 2 characters
      String idSuffix = String.format("%02d", (orderId - 1) * 5 + (i + 1));

      items.add(OrderItem.builder()
          .id(withId ? idSuffix : "")
          .productId(productId + idSuffix)
          .productName((productName + idSuffix).repeat(productNameRepeat))
          .quantity(1 + i)
          .price(new BigDecimal(10.2 + (i * 5.1)).setScale(1, RoundingMode.HALF_UP))
          .build());
    }
    return items;
  }

  /**
   * Creates an order with specified parameters.
   */
  @SneakyThrows
  protected static Order generateOrder(String id, StatusEnum status, String salesChannel,
      int numberOfItems, boolean withOrderItemId,
      String productId, String productName, int productNameRepeat,
      LocalDate orderDate, OffsetDateTime lastUpdateTimestamp) {

    // Generate the list of order items
    List<OrderItem> items = generateOrderItems(
        (id == null || id.isEmpty()) ? 0 : Integer.parseInt(id),
        withOrderItemId, numberOfItems, productId, productName, productNameRepeat);

    return Order.builder()
        .id(id)
        .salesChannel(salesChannel)
        .status(status)
        .orderDate(orderDate)
        .lastUpdateTimestamp(lastUpdateTimestamp)
        .items(items)
        .build();
  }
}