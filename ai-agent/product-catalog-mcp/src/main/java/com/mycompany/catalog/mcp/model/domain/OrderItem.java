package com.mycompany.catalog.mcp.model.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


/**
 * Domain OrderItem.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderItem {

  private String id;

  private String productId;

  private String productName;

  private Integer quantity;

  private BigDecimal price;

}

