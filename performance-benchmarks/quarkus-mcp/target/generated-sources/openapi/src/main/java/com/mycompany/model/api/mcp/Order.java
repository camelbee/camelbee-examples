package com.mycompany.model.api.mcp;

import com.mycompany.model.api.mcp.OrderItem;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("Order")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-17T00:33:14.951701+01:00[Europe/Amsterdam]")
public class Order   {
  private @Valid String id;
  private @Valid String salesChannel;
  public enum StatusEnum {

    PENDING(String.valueOf("Pending")), CONFIRMED(String.valueOf("Confirmed")), PROCESSING(String.valueOf("Processing")), SHIPPED(String.valueOf("Shipped")), DELIVERED(String.valueOf("Delivered")), COMPLETED(String.valueOf("Completed")), CANCELED(String.valueOf("Canceled")), RETURNED(String.valueOf("Returned")), FAILED(String.valueOf("Failed")), ON_HOLD(String.valueOf("On Hold"));


    private String value;

    StatusEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static StatusEnum fromString(String s) {
        for (StatusEnum b : StatusEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
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

  private @Valid StatusEnum status;
  private @Valid LocalDate orderDate;
  private @Valid OffsetDateTime lastUpdateTimestamp;
  private @Valid List<OrderItem> items;

  /**
   * Unique order identifier (server-generated for creates)
   **/
  public Order id(String id) {
    this.id = id;
    return this;
  }

  
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Sales channel identifier
   **/
  public Order salesChannel(String salesChannel) {
    this.salesChannel = salesChannel;
    return this;
  }

  
  @JsonProperty("salesChannel")
  @NotNull
  public String getSalesChannel() {
    return salesChannel;
  }

  @JsonProperty("salesChannel")
  public void setSalesChannel(String salesChannel) {
    this.salesChannel = salesChannel;
  }

  /**
   * Current order status
   **/
  public Order status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * Order creation date (server-generated)
   **/
  public Order orderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
    return this;
  }

  
  @JsonProperty("orderDate")
  public LocalDate getOrderDate() {
    return orderDate;
  }

  @JsonProperty("orderDate")
  public void setOrderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
  }

  /**
   * Last update timestamp (server-managed)
   **/
  public Order lastUpdateTimestamp(OffsetDateTime lastUpdateTimestamp) {
    this.lastUpdateTimestamp = lastUpdateTimestamp;
    return this;
  }

  
  @JsonProperty("lastUpdateTimestamp")
  public OffsetDateTime getLastUpdateTimestamp() {
    return lastUpdateTimestamp;
  }

  @JsonProperty("lastUpdateTimestamp")
  public void setLastUpdateTimestamp(OffsetDateTime lastUpdateTimestamp) {
    this.lastUpdateTimestamp = lastUpdateTimestamp;
  }

  /**
   * Array of order items
   **/
  public Order items(List<OrderItem> items) {
    this.items = items;
    return this;
  }

  
  @JsonProperty("items")
 @Size(min=1)  public List<OrderItem> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<OrderItem> items) {
    this.items = items;
  }

  public Order addItemsItem(OrderItem itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }

    this.items.add(itemsItem);
    return this;
  }

  public Order removeItemsItem(OrderItem itemsItem) {
    if (itemsItem != null && this.items != null) {
      this.items.remove(itemsItem);
    }

    return this;
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

