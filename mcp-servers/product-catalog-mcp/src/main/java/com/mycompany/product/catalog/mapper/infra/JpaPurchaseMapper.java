package com.mycompany.product.catalog.mapper.infra;

import com.mycompany.product.catalog.config.SharedMapperConfig;
import com.mycompany.product.catalog.model.domain.Order;
import com.mycompany.product.catalog.model.domain.OrderItem;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Domain Order/OrderItem to Infra Json Purchase/PurchaseItem and vice versa.
 *
 * @author camelbee
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JpaPurchaseMapper {

  // Json Purchase to Domain Order
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "purchaseDate", target = "orderDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp")
  @Mapping(source = "items", target = "items")
  Order jpaPurchaseToDomainOrder(com.mycompany.product.catalog.model.infra.jpa.postgresql.Purchase purchase);

  // Domain Order to Json Purchase
  @Mapping(source = "id", target = "id", qualifiedByName = "safeStringToInteger")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "orderDate", target = "purchaseDate", defaultExpression = "java(java.time.LocalDate.now())")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp", defaultExpression = "java(java.time.LocalDateTime.now())")
  @Mapping(source = "items", target = "items")
  com.mycompany.product.catalog.model.infra.jpa.postgresql.Purchase domainOrderToJsonPurchase(Order order);

  // Json PurchaseItem to Domain OrderItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  OrderItem jpaPurchaseItemToDomainOrderItem(com.mycompany.product.catalog.model.infra.jpa.postgresql.PurchaseItem purchaseItem);

  // Domain OrderItem to Json PurchaseItem
  @Mapping(source = "id", target = "id", qualifiedByName = "safeStringToInteger")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  com.mycompany.product.catalog.model.infra.jpa.postgresql.PurchaseItem domainOrderItemToJsonPurchaseItem(OrderItem orderItem);

  // Order List mappings
  List<Order> jpaPurchasesToDomainOrders(List<com.mycompany.product.catalog.model.infra.jpa.postgresql.Purchase> purchases);

  List<com.mycompany.product.catalog.model.infra.jpa.postgresql.Purchase> domainOrdersToJsonPurchases(List<Order> orders);

  // OrderItem List mappings
  List<OrderItem> jpaToDomainOrderItems(List<com.mycompany.product.catalog.model.infra.jpa.postgresql.PurchaseItem> purchaseItems);

  List<com.mycompany.product.catalog.model.infra.jpa.postgresql.PurchaseItem> domainToJsonOrderItems(List<OrderItem> orderItems);

  // Custom mapping method
  default OffsetDateTime map(LocalDateTime value) {
    return value != null ? value.atOffset(ZoneOffset.UTC) : null;
  }

  default LocalDateTime map(OffsetDateTime value) {
    return value != null ? value.toLocalDateTime() : null;
  }

  /**
   * SafeStringToInteger.
   *
   * @param value The string value.
   * @return Integer.
   */

  @Named("safeStringToInteger")
  default Long safeStringToInteger(String value) {
    try {
      return value != null ? Long.parseLong(value) : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
