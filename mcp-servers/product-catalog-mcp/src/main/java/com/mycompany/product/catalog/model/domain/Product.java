package com.mycompany.product.catalog.model.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Domain Product.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Product {

  private String id;

  private String name;

  private String description;

  private String category;

  private BigDecimal price;

  private String currency;

  private Boolean inStock;

  private String imageUrl;

}
