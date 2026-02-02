package com.mycompany.model.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
