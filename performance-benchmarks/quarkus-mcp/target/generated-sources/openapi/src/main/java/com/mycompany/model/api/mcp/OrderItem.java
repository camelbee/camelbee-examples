package com.mycompany.model.api.mcp;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("OrderItem")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-17T00:33:14.951701+01:00[Europe/Amsterdam]")
public class OrderItem   {
  private @Valid String id;
  private @Valid String productId;
  private @Valid String productName;
  private @Valid Integer quantity = 1;
  private @Valid BigDecimal price;

  /**
   * Order item identifier (server-generated)
   **/
  public OrderItem id(String id) {
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
   * Product identifier
   **/
  public OrderItem productId(String productId) {
    this.productId = productId;
    return this;
  }

  
  @JsonProperty("productId")
  public String getProductId() {
    return productId;
  }

  @JsonProperty("productId")
  public void setProductId(String productId) {
    this.productId = productId;
  }

  /**
   * Product name
   **/
  public OrderItem productName(String productName) {
    this.productName = productName;
    return this;
  }

  
  @JsonProperty("productName")
  @NotNull
  public String getProductName() {
    return productName;
  }

  @JsonProperty("productName")
  public void setProductName(String productName) {
    this.productName = productName;
  }

  /**
   * Quantity ordered
   * minimum: 1
   **/
  public OrderItem quantity(Integer quantity) {
    this.quantity = quantity;
    return this;
  }

  
  @JsonProperty("quantity")
 @Min(1)  public Integer getQuantity() {
    return quantity;
  }

  @JsonProperty("quantity")
  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  /**
   * Price per unit
   * minimum: 0
   **/
  public OrderItem price(BigDecimal price) {
    this.price = price;
    return this;
  }

  
  @JsonProperty("price")
 @DecimalMin("0")  public BigDecimal getPrice() {
    return price;
  }

  @JsonProperty("price")
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

