package com.mycompany.model.api.mcp;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mycompany.model.api.mcp.OrderItem;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Order
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-02-19T08:56:49.692675+01:00[Europe/Amsterdam]")
public class Order {

  private String id;

  private String salesChannel;

  /**
   * Current order status
   */
  public enum StatusEnum {
    PENDING("Pending"),
    
    CONFIRMED("Confirmed"),
    
    PROCESSING("Processing"),
    
    SHIPPED("Shipped"),
    
    DELIVERED("Delivered"),
    
    COMPLETED("Completed"),
    
    CANCELED("Canceled"),
    
    RETURNED("Returned"),
    
    FAILED("Failed"),
    
    ON_HOLD("On Hold");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private StatusEnum status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate orderDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime lastUpdateTimestamp;

  @Valid
  private List<@Valid OrderItem> items;

  public Order() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public Order(String salesChannel) {
    this.salesChannel = salesChannel;
  }

  public Order id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Unique order identifier (server-generated for creates)
   * @return id
  */
  
  @Schema(name = "id", accessMode = Schema.AccessMode.READ_ONLY, example = "order-12345", description = "Unique order identifier (server-generated for creates)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Order salesChannel(String salesChannel) {
    this.salesChannel = salesChannel;
    return this;
  }

  /**
   * Sales channel identifier
   * @return salesChannel
  */
  @NotNull 
  @Schema(name = "salesChannel", example = "mcp-agent", description = "Sales channel identifier", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("salesChannel")
  public String getSalesChannel() {
    return salesChannel;
  }

  public void setSalesChannel(String salesChannel) {
    this.salesChannel = salesChannel;
  }

  public Order status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Current order status
   * @return status
  */
  
  @Schema(name = "status", description = "Current order status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public Order orderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
    return this;
  }

  /**
   * Order creation date (server-generated)
   * @return orderDate
  */
  @Valid 
  @Schema(name = "orderDate", accessMode = Schema.AccessMode.READ_ONLY, example = "Wed Jul 02 02:00:00 CEST 2025", description = "Order creation date (server-generated)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("orderDate")
  public LocalDate getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
  }

  public Order lastUpdateTimestamp(OffsetDateTime lastUpdateTimestamp) {
    this.lastUpdateTimestamp = lastUpdateTimestamp;
    return this;
  }

  /**
   * Last update timestamp (server-managed)
   * @return lastUpdateTimestamp
  */
  @Valid 
  @Schema(name = "lastUpdateTimestamp", accessMode = Schema.AccessMode.READ_ONLY, example = "2025-07-02T10:30Z", description = "Last update timestamp (server-managed)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lastUpdateTimestamp")
  public OffsetDateTime getLastUpdateTimestamp() {
    return lastUpdateTimestamp;
  }

  public void setLastUpdateTimestamp(OffsetDateTime lastUpdateTimestamp) {
    this.lastUpdateTimestamp = lastUpdateTimestamp;
  }

  public Order items(List<@Valid OrderItem> items) {
    this.items = items;
    return this;
  }

  public Order addItemsItem(OrderItem itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }
    this.items.add(itemsItem);
    return this;
  }

  /**
   * Array of order items
   * @return items
  */
  @Valid @Size(min = 1) 
  @Schema(name = "items", description = "Array of order items", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("items")
  public List<@Valid OrderItem> getItems() {
    return items;
  }

  public void setItems(List<@Valid OrderItem> items) {
    this.items = items;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Order order = (Order) o;
    return Objects.equals(this.id, order.id) &&
        Objects.equals(this.salesChannel, order.salesChannel) &&
        Objects.equals(this.status, order.status) &&
        Objects.equals(this.orderDate, order.orderDate) &&
        Objects.equals(this.lastUpdateTimestamp, order.lastUpdateTimestamp) &&
        Objects.equals(this.items, order.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, salesChannel, status, orderDate, lastUpdateTimestamp, items);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Order {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    salesChannel: ").append(toIndentedString(salesChannel)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    orderDate: ").append(toIndentedString(orderDate)).append("\n");
    sb.append("    lastUpdateTimestamp: ").append(toIndentedString(lastUpdateTimestamp)).append("\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

