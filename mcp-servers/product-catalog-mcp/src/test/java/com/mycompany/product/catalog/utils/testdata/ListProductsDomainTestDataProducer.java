package com.mycompany.product.catalog.utils.testdata;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates test data for the List Products operation.
 */
public class ListProductsDomainTestDataProducer extends BaseDomainTestDataProducer {

  public static final class RequestScenarios {

    public static final String LIST_PRODUCTS_SUCCESS_PAGE_1 = "listproducts-success-page-1-request";
    public static final String LIST_PRODUCTS_SUCCESS_PAGE_2 = "listproducts-success-page-2-request";
    public static final String LIST_PRODUCTS_ERROR_REST_500 = "listproducts-backend-error-rest-500-request";
  }

  public static List<RequestResponseScenario> generateListProductsRequests() {
    List<RequestResponseScenario> scenarios = new ArrayList<>();

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.LIST_PRODUCTS_SUCCESS_PAGE_1)
        .page("1").pageSize("5")
        .transactionId(TXID_FOR_SUCCESS).build());

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.LIST_PRODUCTS_SUCCESS_PAGE_2)
        .page("2").pageSize("5")
        .transactionId(TXID_FOR_SUCCESS).build());

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.LIST_PRODUCTS_ERROR_REST_500)
        .page("1").pageSize("5")
        .transactionId(TXID_FOR_REST_500_ERROR).build());

    return scenarios;
  }
}
