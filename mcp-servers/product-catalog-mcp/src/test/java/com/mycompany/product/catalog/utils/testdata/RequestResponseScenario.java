package com.mycompany.product.catalog.utils.testdata;

import com.mycompany.product.catalog.model.domain.Error;
import com.mycompany.product.catalog.model.domain.PaginatedResponse;
import com.mycompany.product.catalog.model.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestResponseScenario {

  private String name;
  private Product product;
  private PaginatedResponse paginatedResponse;
  private Error error;
  private String page;
  private String pageSize;
  private String query;
  private String category;
  private String productId;
  private String transactionId;

}
