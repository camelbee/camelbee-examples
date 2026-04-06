package com.mycompany.catalog.mcp.utils.testdata;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates test data for ListProducts operation.
 */
public class ListProductsDomainTestDataProducer extends BaseDomainTestDataProducer {

  public static class RequestScenarios {

    public static final String LIST_PRODUCTS_SUCCESS_PAGE_1 = "LIST_PRODUCTS_SUCCESS_PAGE_1";
    public static final String LIST_PRODUCTS_ERROR_INVALID_PAGE = "LIST_PRODUCTS_ERROR_INVALID_PAGE";
    public static final String LIST_PRODUCTS_ERROR_REST_400 = "LIST_PRODUCTS_ERROR_REST_400";
    public static final String LIST_PRODUCTS_ERROR_REST_500 = "LIST_PRODUCTS_ERROR_REST_500";
  }

  public static List<RequestResponseScenario> generateListProductsRequests() {
    List<RequestResponseScenario> scenarios = new ArrayList<>();

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.LIST_PRODUCTS_SUCCESS_PAGE_1)
        .productPage(generateProductPage(generateProducts(3), 1, 10, 1, 3))
        .page("1").pageSize("10")
        .transactionId(TXID_FOR_SUCCESS)
        .build());

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.LIST_PRODUCTS_ERROR_INVALID_PAGE)
        .page("0").pageSize("10")
        .transactionId(TXID_FOR_SUCCESS)
        .build());

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.LIST_PRODUCTS_ERROR_REST_400)
        .page("1").pageSize("10")
        .transactionId(TXID_FOR_REST_400_ERROR)
        .build());

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.LIST_PRODUCTS_ERROR_REST_500)
        .page("1").pageSize("10")
        .transactionId(TXID_FOR_REST_500_ERROR)
        .build());

    return scenarios;
  }

}
