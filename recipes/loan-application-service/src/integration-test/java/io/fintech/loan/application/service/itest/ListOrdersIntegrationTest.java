package io.fintech.loan.application.service.itest;

import static org.assertj.core.api.Assertions.assertThat;

import io.fintech.loan.application.service.model.domain.Order;
import java.util.Arrays;
import java.util.List;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * Integration test for REST interface ListOrders operation.
 */

public class ListOrdersIntegrationTest extends IntegrationTest {

  protected static final String LISTORDERS_BASE_PATH_API = BASE_PATH_API + "%s/listorders/";
  protected static final String LISTORDERS_BASE_PATH_INFRA = BASE_PATH_INFRA + "%s/listpurchases/";

  protected String lastNextCursor = "";

  @EndpointInject("mock:captureListOrdersJpa")
  protected MockEndpoint captureListOrdersJpa;

  private static boolean initialized = false;

  public void setup() throws Exception {

    captureMockEndpoints = Arrays.asList(
        captureError,
        captureListOrdersJpa
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

      // add a new endpoint at the end of the jpa route
      AdviceWith.adviceWith(camelContext, "listOrdersJpaRoute",
          a -> a.weaveAddLast().to("mock:captureListOrdersJpa"));

    }

    initialized = true;

    super.resetBeforeAll();
  }

  protected void setupListOrdersSuccessScenario() throws Exception {

    resetAllMockedEndpoints();

    captureError.expectedMessageCount(0);

    captureListOrdersJpa.expectedMessageCount(1);

  }

  protected void validateListOrdersSuccessScenario(String fileName, String page, String pageSize, String salesChannel, int expectedOrders) throws Exception {

    String requestFile = LISTORDERS_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase");

    captureError.assertIsSatisfied();

    captureListOrdersJpa.assertIsSatisfied();
    assertThat((List<Order>) captureListOrdersJpa.getReceivedExchanges().get(0).getIn().getBody()).hasSize(expectedOrders);

  }

  protected void setupListOrdersBadRequestFromTheInterfaceScenario() throws Exception {

    resetAllMockedEndpoints();

    captureError.expectedMessageCount(1);

    captureListOrdersJpa.expectedMessageCount(0);

  }

  protected void validateBadRequestFromTheInterfaceScenario() throws Exception {

    captureError.assertIsSatisfied();

    captureListOrdersJpa.assertIsSatisfied();

  }

}
