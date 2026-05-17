package io.fintech.loan.application.service.mapper.infra;

import io.fintech.loan.application.service.config.SharedMapperConfig;
import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.model.domain.OrderItem;
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
 * Mapper for converting Domain Order/OrderItem to Infra Cache Purchase/PurchaseItem and vice versa.
 *
 * @author camelbee
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CachePurchaseMapper {

  // CPD-OFF
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
  @Mapping(source = "purchaseDate", target = "orderDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp")
  @Mapping(source = "items", target = "items")
  Order cachePurchaseToDomainOrder(io.fintech.loan.application.service.model.infra.cache.Purchase purchase);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
  @Mapping(source = "orderDate", target = "purchaseDate", defaultExpression = "java(java.time.LocalDate.now())")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp", defaultExpression = "java(java.time.LocalDateTime.now())")
  @Mapping(source = "items", target = "items")
  io.fintech.loan.application.service.model.infra.cache.Purchase domainOrderToCachePurchase(Order order);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  OrderItem cachePurchaseItemToDomainOrderItem(io.fintech.loan.application.service.model.infra.cache.PurchaseItem purchaseItem);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  io.fintech.loan.application.service.model.infra.cache.PurchaseItem domainOrderItemToCachePurchaseItem(OrderItem orderItem);

  List<Order> cachePurchasesToDomainOrders(List<io.fintech.loan.application.service.model.infra.cache.Purchase> purchases);

  List<io.fintech.loan.application.service.model.infra.cache.Purchase> domainOrdersToCachePurchases(List<Order> orders);

  List<OrderItem> cacheToDomainOrderItems(List<io.fintech.loan.application.service.model.infra.cache.PurchaseItem> purchaseItems);

  List<io.fintech.loan.application.service.model.infra.cache.PurchaseItem> domainToCacheOrderItems(List<OrderItem> orderItems);

  @Named("mapStatus")
  default Order.StatusEnum mapStatus(String status) {
    return Order.StatusEnum.fromValue(status);
  }

  @Named("mapStatus")
  default String mapStatus(Order.StatusEnum value) {
    return value != null ? value.name() : null;
  }

  default OffsetDateTime map(LocalDateTime value) {
    return value != null ? value.atOffset(ZoneOffset.UTC) : null;
  }

  default LocalDateTime map(OffsetDateTime value) {
    return value != null ? value.toLocalDateTime() : null;
  }

}