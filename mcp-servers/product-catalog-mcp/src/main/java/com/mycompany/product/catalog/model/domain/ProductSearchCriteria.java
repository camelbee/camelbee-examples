package com.mycompany.product.catalog.model.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Domain ProductSearchCriteria.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ProductSearchCriteria {

  private String query;

  private String category;

  private BigDecimal minPrice;

  private BigDecimal maxPrice;

  private Boolean inStock;

  private Integer page;

  private Integer pageSize;

}
