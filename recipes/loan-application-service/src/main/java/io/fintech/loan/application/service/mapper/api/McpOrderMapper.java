package io.fintech.loan.application.service.mapper.api;

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
import org.mapstruct.NullValueCheckStrategy;

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
  Order mcpToDomainOrder(io.fintech.loan.application.service.model.api.mcp.Order order);

  // Domain Order to Mcp Order
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "orderDate", target = "orderDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp")
  @Mapping(source = "items", target = "items")
  io.fintech.loan.application.service.model.api.mcp.Order domainToMcpOrder(Order order);

  // Mcp OrderItem to Domain OrderItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  OrderItem mcpToDomainOrderItem(io.fintech.loan.application.service.model.api.mcp.OrderItem orderItem);

  // Domain OrderItem to Mcp OrderItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  io.fintech.loan.application.service.model.api.mcp.OrderItem domainToMcpOrderItem(OrderItem orderItem);

  // Order List mappings
  List<Order> mcpToDomainOrders(List<io.fintech.loan.application.service.model.api.mcp.Order> orders);

  List<io.fintech.loan.application.service.model.api.mcp.Order> domainToMcpOrders(List<Order> orders);

  // OrderItem List mappings
  List<OrderItem> mcpToDomainOrderItems(List<io.fintech.loan.application.service.model.api.mcp.OrderItem> orderItems);

  List<io.fintech.loan.application.service.model.api.mcp.OrderItem> domainToMcpOrderItems(List<OrderItem> orderItems);

  // Custom mapping method
  default OffsetDateTime map(LocalDateTime value) {
    return value != null ? value.atOffset(ZoneOffset.UTC) : null;
  }

  default LocalDateTime map(OffsetDateTime value) {
    return value != null ? value.toLocalDateTime() : null;
  }

}
