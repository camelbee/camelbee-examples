package com.mycompany.catalog.mcp.mapper.api;

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
 * Mapper for converting Api Mcp Order/OrderItem To Domain Order/OrderItem to and vice versa.
 *
 * @author camelbee
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface McpOrderMapper {

  // CPD-OFF
  // Mcp Order to Domain Order
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "orderDate", target = "orderDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp")
  @Mapping(source = "items", target = "items")
  Order mcpToDomainOrder(com.mycompany.catalog.mcp.model.api.mcp.Order order);

  // Domain Order to Mcp Order
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "orderDate", target = "orderDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp")
  @Mapping(source = "items", target = "items")
  com.mycompany.catalog.mcp.model.api.mcp.Order domainToMcpOrder(Order order);

  // Mcp OrderItem to Domain OrderItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  OrderItem mcpToDomainOrderItem(com.mycompany.catalog.mcp.model.api.mcp.OrderItem orderItem);

  // Domain OrderItem to Mcp OrderItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  com.mycompany.catalog.mcp.model.api.mcp.OrderItem domainToMcpOrderItem(OrderItem orderItem);

  // Order List mappings
  List<Order> mcpToDomainOrders(List<com.mycompany.catalog.mcp.model.api.mcp.Order> orders);

  List<com.mycompany.catalog.mcp.model.api.mcp.Order> domainToMcpOrders(List<Order> orders);

  // OrderItem List mappings
  List<OrderItem> mcpToDomainOrderItems(List<com.mycompany.catalog.mcp.model.api.mcp.OrderItem> orderItems);

  List<com.mycompany.catalog.mcp.model.api.mcp.OrderItem> domainToMcpOrderItems(List<OrderItem> orderItems);

  // Custom mapping method
  default OffsetDateTime map(LocalDateTime value) {
    return value != null ? value.atOffset(ZoneOffset.UTC) : null;
  }

  default LocalDateTime map(OffsetDateTime value) {
    return value != null ? value.toLocalDateTime() : null;
  }


}
