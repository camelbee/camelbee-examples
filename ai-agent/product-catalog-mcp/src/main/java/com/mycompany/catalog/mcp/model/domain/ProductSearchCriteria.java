package com.mycompany.catalog.mcp.model.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain ProductSearchCriteria - filters for product search.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductSearchCriteria {

  private String query;

  private String category;

  private BigDecimal minPrice;

  private BigDecimal maxPrice;

  private Boolean inStock;

  private Integer page;

  private Integer pageSize;

}
