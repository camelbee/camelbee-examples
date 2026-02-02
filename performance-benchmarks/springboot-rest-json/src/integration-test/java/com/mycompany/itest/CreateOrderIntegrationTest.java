package com.mycompany.itest;

import java.util.Arrays;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;

/**
 * Integration test for REST interface CreateOrder operation.
 */

public class CreateOrderIntegrationTest extends IntegrationTest {

  protected static final String CREATEORDER_BASE_PATH_API = BASE_PATH_API + "%s/createorder/";
  protected static final String CREATEORDER_BASE_PATH_INFRA = BASE_PATH_INFRA + "%s/createpurchase/";

  @EndpointInject("mock:captureCreateOrderMock")
  protected MockEndpoint captureCreateOrderMock;

  private static boolean initialized = false;

  public void setup() throws Exception {

    captureMockEndpoints = Arrays.asList(
        captureError,
        captureCreateOrderMock
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

      var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);

      // add a new endpoint at the end of the mock route
      AdviceWith.adviceWith(camelContext, "createOrderMockRoute",
          a -> a.weaveAddLast().to("mock:captureCreateOrderMock"));

    }

    initialized = true;

  }

  protected void setupCreateOrderSuccessScenario(String fileName) throws Exception {

    resetAllMockedEndpoints();

    String requestFile = CREATEORDER_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase");

    captureError.expectedMessageCount(0);

    captureCreateOrderMock.expectedMessageCount(1);

  }

  protected void validateCreateOrderSuccessScenario(String fileName) throws Exception {

    String requestFile = CREATEORDER_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase");

    captureError.assertIsSatisfied();

    captureCreateOrderMock.assertIsSatisfied();

  }

  protected void setupCreateOrderBadRequestFromTheInterfaceScenario(String fileName, int interfaceRetryCount, boolean globalErrorHandlerUsed) throws Exception {

    resetAllMockedEndpoints();

    String requestFile = CREATEORDER_BASE_PATH_INFRA + fileName.replace("createorder", "createpurchase");

    captureError.expectedMessageCount(globalErrorHandlerUsed ? interfaceRetryCount : 0);

    captureCreateOrderMock.expectedMessageCount(0);

  }

  protected void validateBadRequestFromTheInterfaceScenario() throws Exception {
    /*
    check if the captureError satisfied
    you can even go further to check the error captured via captureError.getReceivedExhanges(0).getError to check the type of error
     */
    captureError.assertIsSatisfied();

    captureCreateOrderMock.assertIsSatisfied();

  }

}
