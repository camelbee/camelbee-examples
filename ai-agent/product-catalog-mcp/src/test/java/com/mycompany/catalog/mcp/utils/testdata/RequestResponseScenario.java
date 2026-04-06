package com.mycompany.catalog.mcp.utils.testdata;

import com.mycompany.catalog.mcp.model.domain.Error;
import com.mycompany.catalog.mcp.model.domain.Order;
import java.util.List;
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
  private Order order;
  private List<Order> orders;
  private Error error;
  private String page;
  private String pageSize;
  private String salesChannel;
  private String status;
  private String transactionId;

}
