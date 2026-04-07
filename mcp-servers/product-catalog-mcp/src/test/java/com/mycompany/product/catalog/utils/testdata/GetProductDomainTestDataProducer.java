package com.mycompany.product.catalog.utils.testdata;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates test data for the Get Product operation.
 */
public class GetProductDomainTestDataProducer extends BaseDomainTestDataProducer {

  public static final class RequestScenarios {

    public static final String GET_PRODUCT_SUCCESS = "getproduct-success-request";
    public static final String GET_PRODUCT_NOT_FOUND = "getproduct-notfound-request";
    public static final String GET_PRODUCT_EMPTY_ID = "getproduct-emptyid-error-request";
  }

  public static List<RequestResponseScenario> generateGetProductRequests() {
    List<RequestResponseScenario> scenarios = new ArrayList<>();

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.GET_PRODUCT_SUCCESS)
        .productId("prod-001")
        .transactionId(TXID_FOR_SUCCESS).build());

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.GET_PRODUCT_NOT_FOUND)
        .productId("nonexistent")
        .transactionId(TXID_FOR_SUCCESS).build());

    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.GET_PRODUCT_EMPTY_ID)
        .productId("")
        .transactionId(TXID_FOR_SUCCESS).build());

    return scenarios;
  }
}
