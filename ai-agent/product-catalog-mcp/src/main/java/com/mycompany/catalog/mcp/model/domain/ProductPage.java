package com.mycompany.catalog.mcp.model.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain ProductPage - paginated product result.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductPage {

  private List<Product> products;

  private Integer page;

  private Integer pageSize;

  private Integer totalPages;

  private Integer totalItems;

}
