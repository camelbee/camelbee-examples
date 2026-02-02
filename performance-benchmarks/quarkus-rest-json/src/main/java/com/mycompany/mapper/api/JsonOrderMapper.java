package com.mycompany.mapper.api;

import com.mycompany.config.SharedMapperConfig;
import com.mycompany.model.domain.Order;
import com.mycompany.model.domain.OrderItem;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Api Json Order/OrderItem To Domain Order/OrderItem to and vice versa.
 *
 * @author camelbee
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JsonOrderMapper {

  // CPD-OFF
  // Json Order to Domain Order
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "orderDate", target = "orderDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp")
  @Mapping(source = "items", target = "items")
  Order jsonToDomainOrder(com.mycompany.model.api.json.Order order);

  // Domain Order to Json Order
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "orderDate", target = "orderDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp")
  @Mapping(source = "items", target = "items")
  com.mycompany.model.api.json.Order domainToJsonOrder(Order order);

  // Json OrderItem to Domain OrderItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  OrderItem jsonToDomainOrderItem(com.mycompany.model.api.json.OrderItem orderItem);

  // Domain OrderItem to Json OrderItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  com.mycompany.model.api.json.OrderItem domainToJsonOrderItem(OrderItem orderItem);

  // Order List mappings
  List<Order> jsonToDomainOrders(List<com.mycompany.model.api.json.Order> orders);

  List<com.mycompany.model.api.json.Order> domainToJsonOrders(List<Order> orders);

  // OrderItem List mappings
  List<OrderItem> jsonToDomainOrderItems(List<com.mycompany.model.api.json.OrderItem> orderItems);

  List<com.mycompany.model.api.json.OrderItem> domainToJsonOrderItems(List<OrderItem> orderItems);

  // Custom mapping method
  default OffsetDateTime map(LocalDateTime value) {
    return value != null ? value.atOffset(ZoneOffset.UTC) : null;
  }

  default LocalDateTime map(OffsetDateTime value) {
    return value != null ? value.toLocalDateTime() : null;
  }

}
