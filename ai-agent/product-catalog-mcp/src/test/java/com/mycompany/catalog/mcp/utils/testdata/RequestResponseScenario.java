package com.mycompany.catalog.mcp.utils.testdata;

import com.mycompany.catalog.mcp.model.domain.Error;
import com.mycompany.catalog.mcp.model.domain.Product;
import com.mycompany.catalog.mcp.model.domain.ProductPage;
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
  private ProductPage productPage;
  private Error error;
  private String page;
  private String pageSize;
  private String transactionId;

}
