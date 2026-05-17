package io.fintech.loan.application.service.itest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Objects;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * Integration test for REST interface GetOrder operation.
 */

public class GetOrderIntegrationTest extends IntegrationTest {

  protected static final String GETORDER_BASE_PATH_API = BASE_PATH_API + "%s/getorder/";
  protected static final String GETORDER_BASE_PATH_INFRA = BASE_PATH_INFRA + "%s/getpurchase/";

  @EndpointInject("mock:captureGetOrderJpa")
  protected MockEndpoint captureGetOrderJpa;

  private static boolean initialized = false;

  public void setup() throws Exception {

    captureMockEndpoints = Arrays.asList(
        captureError,
        captureGetOrderJpa
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
      AdviceWith.adviceWith(camelContext, "getOrderJpaRoute",
          a -> a.weaveAddLast().to("mock:captureGetOrderJpa"));

    }
    initialized = true;

    super.resetBeforeAll();
  }

  protected void setupGetOrderSuccessScenario() throws Exception {

    resetAllMockedEndpoints();

    captureError.expectedMessageCount(0);

    captureGetOrderJpa.expectedMessageCount(1);

  }

  protected void validateGetOrderSuccessScenario(String fileName, String orderId, String salesChannel) throws Exception {

    String requestFile = GETORDER_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase");

    captureError.assertIsSatisfied();

    captureGetOrderJpa.assertIsSatisfied();
    validateOrder(captureGetOrderJpa.getReceivedExchanges().get(0).getIn().getBody(io.fintech.loan.application.service.model.domain.Order.class));

  }

  protected void setupGetOrderNotFoundScenario() throws Exception {

    resetAllMockedEndpoints();

    captureError.expectedMessageCount(1);

    captureGetOrderJpa.expectedMessageCount(0);

  }

  protected void validateGetOrderNotFoundScenario(String orderId, String salesChannel) throws Exception {

    captureError.assertIsSatisfied();

    captureGetOrderJpa.assertIsSatisfied();

  }

  protected void setupGetOrderBadRequestFromTheInterfaceScenario() throws Exception {

    resetAllMockedEndpoints();

    captureError.expectedMessageCount(1);

    captureGetOrderJpa.expectedMessageCount(0);

  }

  protected void validateBadRequestFromTheInterfaceScenario() throws Exception {

    captureError.assertIsSatisfied();

    captureGetOrderJpa.assertIsSatisfied();

  }

  protected void setupGetOrderEmptyPathParameterFromTheInterfaceScenario() throws Exception {

    resetAllMockedEndpoints();

    captureError.expectedMessageCount(1);

    captureGetOrderJpa.expectedMessageCount(0);

  }

  protected void validateEmptyPathParameterFromTheInterfaceScenario() throws Exception {

    captureError.assertIsSatisfied();

    captureGetOrderJpa.assertIsSatisfied();

  }

  private void validateOrder(
      io.fintech.loan.application.service.model.domain.Order order) {

    assertThat(order.getId()).isNotEmpty();
    assertThat(order.getOrderDate()).isNotNull();
    assertThat(order.getLastUpdateTimestamp()).isNotNull();
    assertThat(order.getItems()).hasSize(5);
    assertThat(order.getItems().stream().map(item -> item.getId().toString()).toList()).allMatch(Objects::nonNull);
  }

}
