package com.mycompany.product.catalog.utils.testdata;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates test data for the Search Products operation.
 */
public class SearchProductsDomainTestDataProducer extends BaseDomainTestDataProducer {

  public static final class RequestScenarios {

    public static final String SEARCH_PRODUCTS_SUCCESS = "searchproducts-success-request";
    public static final String SEARCH_PRODUCTS_ERROR_REST_500 = "searchproducts-backend-error-rest-500-request";
  }

  public static List<RequestResponseScenario> generateSearchProductsRequests() {
    List<RequestResponseScenario> scenarios = new ArrayList<>();

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.SEARCH_PRODUCTS_SUCCESS)
        .query("wireless").category("Electronics")
        .page("1").pageSize("10")
        .transactionId(TXID_FOR_SUCCESS).build());

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.SEARCH_PRODUCTS_ERROR_REST_500)
        .query("error").page("1").pageSize("10")
        .transactionId(TXID_FOR_REST_500_ERROR).build());

    return scenarios;
  }
}
