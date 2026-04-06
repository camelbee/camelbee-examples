package com.mycompany.catalog.mcp.mapper.infra;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.NullValueCheckStrategy;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.mycompany.catalog.mcp.config.SharedMapperConfig;
import com.mycompany.catalog.mcp.model.domain.Order;
import com.mycompany.catalog.mcp.model.domain.OrderItem;

/**
 * Mapper for converting Domain Order/OrderItem to Infra Json Purchase/PurchaseItem and vice versa.
 *
 * @author camelbee
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JsonPurchaseMapper {

  // Json Purchase to Domain Order
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "purchaseDate", target = "orderDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp")
  @Mapping(source = "items", target = "items")
  Order jsonPurchaseToDomainOrder(com.mycompany.catalog.mcp.model.infra.json.Purchase purchase);

  // Domain Order to Json Purchase
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "orderDate", target = "purchaseDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp")
  @Mapping(source = "items", target = "items")
  com.mycompany.catalog.mcp.model.infra.json.Purchase domainOrderToJsonPurchase(Order order);

  // Json PurchaseItem to Domain OrderItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  OrderItem jsonPurchaseItemToDomainOrderItem(com.mycompany.catalog.mcp.model.infra.json.PurchaseItem purchaseItem);

  // Domain OrderItem to Json PurchaseItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  com.mycompany.catalog.mcp.model.infra.json.PurchaseItem domainOrderItemToJsonPurchaseItem(OrderItem orderItem);

  // Order List mappings
  List<Order> jsonPurchasesToDomainOrders(List<com.mycompany.catalog.mcp.model.infra.json.Purchase> purchases);

  List<com.mycompany.catalog.mcp.model.infra.json.Purchase> domainOrdersToJsonPurchases(List<Order> orders);

  // OrderItem List mappings
  List<OrderItem> jsonToDomainOrderItems(List<com.mycompany.catalog.mcp.model.infra.json.PurchaseItem> purchaseItems);

  List<com.mycompany.catalog.mcp.model.infra.json.PurchaseItem> domainToJsonOrderItems(List<OrderItem> orderItems);

  // Custom mapping method
  default OffsetDateTime map(LocalDateTime value) {
    return value != null ? value.atOffset(ZoneOffset.UTC) : null;
  }

  default LocalDateTime map(OffsetDateTime value) {
    return value != null ? value.toLocalDateTime() : null;
  }


}
