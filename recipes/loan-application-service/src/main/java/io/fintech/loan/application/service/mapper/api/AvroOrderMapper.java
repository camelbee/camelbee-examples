package io.fintech.loan.application.service.mapper.api;

import io.fintech.loan.application.service.config.SharedMapperConfig;
import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.model.domain.OrderItem;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Api Avro Order/OrderItem To Domain Order/OrderItem to and vice versa.
 *
 * @author camelbee
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AvroOrderMapper {

  // CPD-OFF
  // Avro Order to Domain Order
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
  @Mapping(source = "orderDate", target = "orderDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp")
  @Mapping(source = "items", target = "items")
  Order avroToDomainOrder(io.fintech.loan.application.service.model.api.avro.Order order);

  // Domain Order to Avro Order
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
  @Mapping(source = "orderDate", target = "orderDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp")
  @Mapping(source = "items", target = "items")
  io.fintech.loan.application.service.model.api.avro.Order domainToAvroOrder(Order order);

  // Avro OrderItem to Domain OrderItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  OrderItem avroToDomainOrderItem(io.fintech.loan.application.service.model.api.avro.OrderItem orderItem);

  // Domain OrderItem to Avro OrderItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  io.fintech.loan.application.service.model.api.avro.OrderItem domainToAvroOrderItem(OrderItem orderItem);

  // Order List mappings
  List<Order> avroToDomainOrders(List<io.fintech.loan.application.service.model.api.avro.Order> orders);

  List<io.fintech.loan.application.service.model.api.avro.Order> domainToAvroOrders(List<Order> orders);

  // OrderItem List mappings
  List<OrderItem> avroToDomainOrderItems(List<io.fintech.loan.application.service.model.api.avro.OrderItem> orderItems);

  List<io.fintech.loan.application.service.model.api.avro.OrderItem> domainToAvroOrderItems(List<OrderItem> orderItems);

  @Named("mapStatus")
  default Order.StatusEnum mapStatus(CharSequence avroStatus) {
    return Order.StatusEnum.fromValue(avroStatus.toString());
  }

  @Named("mapStatus")
  default CharSequence mapStatus(Order.StatusEnum value) {
    return value != null ? value.name() : null;
  }

  default OffsetDateTime map(Instant value) {
    return value != null ? OffsetDateTime.ofInstant(value, ZoneId.systemDefault()) : null;
  }

  default Instant map(OffsetDateTime value) {
    return value != null ? value.toInstant() : null;
  }

  default String map(CharSequence value) {
    return value != null ? value.toString() : null;
  }

  default CharSequence map(String value) {
    return value != null ? value : null;
  }

  /**
   * LocalDate.
   *
   * @param orderDate LocalDate.
   * @return LocalDate.
   */
  default LocalDate map(Integer orderDate) {
    if (orderDate == null) {
      return null;
    }
    return LocalDate.ofEpochDay(orderDate);
  }

  /**
   * LocalDate.
   *
   * @param orderDate LocalDate.
   * @return LocalDate.
   */
  default Integer map(LocalDate orderDate) {
    if (orderDate == null) {
      return null;
    }
    return (int) orderDate.toEpochDay();
  }

  /**
   * LocalDate.
   *
   * @param timestamp LocalDate.
   * @return LocalDate.
   */
  default Instant mapInstant(Long timestamp) {
    if (timestamp == null) {
      return null;
    }
    return Instant.ofEpochMilli(timestamp);
  }

  /**
   * LocalDate.
   *
   * @param timestamp LocalDate.
   * @return LocalDate.
   */
  default Long mapInstant(Instant timestamp) {
    if (timestamp == null) {
      return null;
    }
    return timestamp.toEpochMilli();
  }
}
