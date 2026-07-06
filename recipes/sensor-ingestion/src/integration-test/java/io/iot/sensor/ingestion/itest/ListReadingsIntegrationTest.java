package io.iot.sensor.ingestion.itest;

import java.util.Arrays;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;

public class ListReadingsIntegrationTest extends IntegrationTest {

  protected static final String LIST_BASE_PATH_API = BASE_PATH_API + "%s/list/";

  @EndpointInject("mock:captureListReadingsMongoDb")
  protected MockEndpoint captureListReadingsMongoDb;

  private static boolean initialized = false;

  public void setup() throws Exception {
    captureMockEndpoints = Arrays.asList(captureError, captureListReadingsMongoDb);
    verifyMockEndpoints = Arrays.asList();
    super.setup();

    if (!initialized) {
      AdviceWith.adviceWith(camelContext, "listReadingsMongoDbRoute",
          a -> a.weaveAddLast().to("mock:captureListReadingsMongoDb"));
    }
    initialized = true;
    super.resetBeforeAll();
  }

  protected void setupListSuccessScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(0);
    captureListReadingsMongoDb.expectedMessageCount(1);
  }

  protected void validateListSuccessScenario() throws Exception {
    captureError.assertIsSatisfied();
    captureListReadingsMongoDb.assertIsSatisfied();
  }
}
