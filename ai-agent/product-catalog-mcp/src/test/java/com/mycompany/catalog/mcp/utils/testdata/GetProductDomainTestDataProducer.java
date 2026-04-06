package com.mycompany.catalog.mcp.utils.testdata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates test data for GetProduct operation.
 */
public class GetProductDomainTestDataProducer extends BaseDomainTestDataProducer {

  public static class RequestScenarios {

    public static final String GET_PRODUCT_SUCCESS = "GET_PRODUCT_SUCCESS";
    public static final String GET_PRODUCT_ERROR_EMPTY_ID = "GET_PRODUCT_ERROR_EMPTY_ID";
    public static final String GET_PRODUCT_ERROR_NOT_FOUND = "GET_PRODUCT_ERROR_NOT_FOUND";
  }

  public static List<RequestResponseScenario> generateGetProductRequests() {
    List<RequestResponseScenario> scenarios = new ArrayList<>();

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.GET_PRODUCT_SUCCESS)
        .product(generateProduct("prod-001", "Wireless Mouse",
            "Ergonomic wireless mouse with USB receiver", "Electronics",
            new BigDecimal("29.99"), "USD", true,
            "https://example.com/images/wireless-mouse.jpg"))
        .transactionId(TXID_FOR_SUCCESS)
        .build());

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.GET_PRODUCT_ERROR_EMPTY_ID)
        .transactionId(TXID_FOR_SUCCESS)
        .build());

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.GET_PRODUCT_ERROR_NOT_FOUND)
        .transactionId(TXID_FOR_SUCCESS)
        .build());

    return scenarios;
  }

}
