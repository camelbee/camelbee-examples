package com.mycompany.product.catalog.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.product.catalog.model.domain.Order;
import java.util.Arrays;
import java.util.List;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Integration test for REST interface ListOrders operation.
 */

public class ListOrdersIntegrationTest extends IntegrationTest {

  protected static final String LISTORDERS_BASE_PATH_API = BASE_PATH_API + "%s/listorders/";
  protected static final String LISTORDERS_BASE_PATH_INFRA = BASE_PATH_INFRA + "%s/listpurchases/";

  protected String lastNextCursor = "";

  @ConfigProperty(name = "backend-purchase-rest-api.uri")
  protected String purchaseRestApiUri;

  @EndpointInject("mock:captureListOrdersRest")
  protected MockEndpoint captureListOrdersRest;

  private static boolean initialized = false;

  public void setup() throws Exception {

    captureMockEndpoints = Arrays.asList(
        captureError,
        captureListOrdersRest
    );

    verifyMockEndpoints = Arrays.asList(
    );

    super.setup();

    /*
     * We use a flag to ensure routes are create or advised only once across all test classes
     * that extend this base class. A static initialization block cannot be used here
     * because it would run before Spring injects the camelContext, making it
     * unavailable during class loading.
     */
    if (!initialized) {

      // add a new endpoint at the end of the rest route
      AdviceWith.adviceWith(camelContext, "listOrdersRestRoute",
          a -> a.weaveAddLast().to("mock:captureListOrdersRest"));

    }

    initialized = true;

    super.resetBeforeAll();
  }

  protected void setupListOrdersSuccessScenario() throws Exception {

    resetAllMockedEndpoints();

    captureError.expectedMessageCount(0);

    captureListOrdersRest.expectedMessageCount(1);

  }

  protected void validateListOrdersSuccessScenario(String fileName, String page, String pageSize, String salesChannel, int expectedOrders) throws Exception {

    String requestFile = LISTORDERS_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase");

    captureError.assertIsSatisfied();

    captureListOrdersRest.assertIsSatisfied();
    assertThat((List<Order>) captureListOrdersRest.getReceivedExchanges().get(0).getIn().getBody()).hasSize(expectedOrders);

    wireMock.verify(getRequestedFor(urlPathEqualTo(purchaseRestApiUri))
        .withQueryParam("page", equalTo(page))
        .withQueryParam("pageSize", equalTo(pageSize))
        .withQueryParam("salesChannel", equalTo(salesChannel)));

  }

  protected void setupListOrdersBadRequestFromTheInterfaceScenario() throws Exception {

    resetAllMockedEndpoints();

    captureError.expectedMessageCount(1);

    captureListOrdersRest.expectedMessageCount(0);

  }

  protected void validateBadRequestFromTheInterfaceScenario() throws Exception {

    captureError.assertIsSatisfied();

    captureListOrdersRest.assertIsSatisfied();
    wireMock.verify(0, getRequestedFor(urlEqualTo(purchaseRestApiUri)));

  }

  protected void setupListOrdersErrorFromTheRestBackendScenario() throws Exception {

    resetAllMockedEndpoints();

    captureError.expectedMessageCount(1);

    captureListOrdersRest.expectedMessageCount(0);

  }

  protected void validateErrorFromTheRestBackendScenario(String page, String pageSize, String salesChannel) throws Exception {

    captureError.assertIsSatisfied();

    captureListOrdersRest.assertIsSatisfied();

    wireMock.verify(getRequestedFor(urlPathEqualTo(purchaseRestApiUri))
        .withQueryParam("page", equalTo(page))
        .withQueryParam("pageSize", equalTo(pageSize))
        .withQueryParam("salesChannel", equalTo(salesChannel)));

  }

}
