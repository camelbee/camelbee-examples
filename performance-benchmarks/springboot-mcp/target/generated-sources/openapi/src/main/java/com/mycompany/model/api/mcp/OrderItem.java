package com.mycompany.model.api.mcp;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.math.BigDecimal;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * OrderItem
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-02-19T08:56:49.692675+01:00[Europe/Amsterdam]")
public class OrderItem {

  private String id;

  private String productId;

  private String productName;

  private Integer quantity = 1;

  private BigDecimal price;

  public OrderItem() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public OrderItem(String productName) {
    this.productName = productName;
  }

  public OrderItem id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Order item identifier (server-generated)
   * @return id
  */
  
  @Schema(name = "id", accessMode = Schema.AccessMode.READ_ONLY, example = "item-001", description = "Order item identifier (server-generated)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public OrderItem productId(String productId) {
    this.productId = productId;
    return this;
  }

  /**
   * Product identifier
   * @return productId
  */
  
  @Schema(name = "productId", example = "p1", description = "Product identifier", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("productId")
  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public OrderItem productName(String productName) {
    this.productName = productName;
    return this;
  }

  /**
   * Product name
   * @return productName
  */
  @NotNull 
  @Schema(name = "productName", example = "Widget A", description = "Product name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("productName")
  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public OrderItem quantity(Integer quantity) {
    this.quantity = quantity;
    return this;
  }

  /**
   * Quantity ordered
   * minimum: 1
   * @return quantity
  */
  @Min(1) 
  @Schema(name = "quantity", example = "2", description = "Quantity ordered", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("quantity")
  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public OrderItem price(BigDecimal price) {
    this.price = price;
    return this;
  }

  /**
   * Price per unit
   * minimum: 0
   * @return price
  */
  @Valid @DecimalMin("0") 
  @Schema(name = "price", example = "29.99", description = "Price per unit", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("price")
  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrderItem orderItem = (OrderItem) o;
    return Objects.equals(this.id, orderItem.id) &&
        Objects.equals(this.productId, orderItem.productId) &&
        Objects.equals(this.productName, orderItem.productName) &&
        Objects.equals(this.quantity, orderItem.quantity) &&
        Objects.equals(this.price, orderItem.price);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, productId, productName, quantity, price);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OrderItem {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    productId: ").append(toIndentedString(productId)).append("\n");
    sb.append("    productName: ").append(toIndentedString(productName)).append("\n");
    sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
    sb.append("    price: ").append(toIndentedString(price)).append("\n");
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

