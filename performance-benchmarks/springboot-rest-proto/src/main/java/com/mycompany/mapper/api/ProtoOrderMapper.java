package com.mycompany.mapper.api;

import com.mycompany.config.SharedMapperConfig;
import com.mycompany.model.domain.Order;
import com.mycompany.model.domain.OrderItem;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Api Proto Order/OrderItem To Domain Order/OrderItem to and vice versa.
 *
 * @author camelbee
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ProtoOrderMapper {

  // CPD-OFF
  // Proto Order to Domain Order
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
  @Mapping(source = "orderDate", target = "orderDate", qualifiedByName = "dateToLocalDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp", qualifiedByName = "timestampToOffsetDateTime")
  @Mapping(source = "itemsList", target = "items")
  Order protoToDomainOrder(com.mycompany.model.api.proto.Order order);

  // Domain Order to Proto Order
  @Mapping(source = "id", target = "id")
  @Mapping(source = "salesChannel", target = "salesChannel")
  @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
  @Mapping(source = "orderDate", target = "orderDate", qualifiedByName = "localDateToDate")
  @Mapping(source = "lastUpdateTimestamp", target = "lastUpdateTimestamp", qualifiedByName = "offsetDateTimeToTimestamp")
  @Mapping(source = "items", target = "itemsList")
  com.mycompany.model.api.proto.Order domainToProtoOrder(Order order);

  // Proto OrderItem to Domain OrderItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  OrderItem protoToDomainOrderItem(com.mycompany.model.api.proto.OrderItem orderItem);

  // Domain OrderItem to Proto OrderItem
  @Mapping(source = "id", target = "id")
  @Mapping(source = "productId", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "price", target = "price")
  com.mycompany.model.api.proto.OrderItem domainToProtoOrderItem(OrderItem orderItem);

  // Order List mappings
  List<Order> protoToDomainOrders(List<com.mycompany.model.api.proto.Order> orders);

  List<com.mycompany.model.api.proto.Order> domainToProtoOrders(List<Order> orders);

  // OrderItem List mappings
  List<OrderItem> protoToDomainOrderItems(List<com.mycompany.model.api.proto.OrderItem> orderItems);

  List<com.mycompany.model.api.proto.OrderItem> domainToProtoOrderItems(List<OrderItem> orderItems);

  /**
   * stringToStatusEnum.
   */
  @Named("mapStatus")
  default Order.StatusEnum mapStatus(String protoStatus) {
    if (protoStatus == null) {
      return null;
    }
    return Order.StatusEnum.fromValue(protoStatus);
  }

  /**
   * statusEnumToString.
   */
  @Named("mapStatus")
  default String mapStatus(Order.StatusEnum value) {
    return value != null ? value.name() : null;
  }

  /**
   * localDateToDate.
   */
  @Named("dateToLocalDate")
  default LocalDate dateToLocalDate(com.google.type.Date date) {
    if (date == null) {
      return null;
    }
    return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
  }

  /**
   * localDateToDate.
   */
  @Named("localDateToDate")
  default com.google.type.Date localDateToDate(LocalDate localDate) {
    if (localDate == null) {
      return null;
    }
    return com.google.type.Date.newBuilder()
        .setYear(localDate.getYear())
        .setMonth(localDate.getMonthValue())
        .setDay(localDate.getDayOfMonth())
        .build();
  }

  /**
   * localDateToDate.
   */
  @Named("timestampToOffsetDateTime")
  default OffsetDateTime timestampToOffsetDateTime(com.google.protobuf.Timestamp timestamp) {
    if (timestamp == null) {
      return null;
    }
    return OffsetDateTime.ofInstant(
        Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
        ZoneOffset.UTC
    );
  }

  /**
   * localDateToDate.
   */
  @Named("offsetDateTimeToTimestamp")
  default com.google.protobuf.Timestamp offsetDateTimeToTimestamp(OffsetDateTime offsetDateTime) {
    if (offsetDateTime == null) {
      return null;
    }
    Instant instant = offsetDateTime.toInstant();
    return com.google.protobuf.Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

}
