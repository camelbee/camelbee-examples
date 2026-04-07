package com.mycompany.product.catalog.model.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Domain PaginatedResponse.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PaginatedResponse {

  private List<Product> items;

  private Integer page;

  private Integer pageSize;

  private Integer totalPages;

  private Integer totalItems;

}
