package io.iot.sensor.ingestion.itest;

import java.util.Arrays;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;

public class GetReadingIntegrationTest extends IntegrationTest {

  protected static final String GET_BASE_PATH_API = BASE_PATH_API + "%s/get/";

  @EndpointInject("mock:captureGetReadingMongoDb")
  protected MockEndpoint captureGetReadingMongoDb;

  private static boolean initialized = false;

  public void setup() throws Exception {
    captureMockEndpoints = Arrays.asList(captureError, captureGetReadingMongoDb);
    verifyMockEndpoints = Arrays.asList();
    super.setup();

    if (!initialized) {
      AdviceWith.adviceWith(camelContext, "getReadingMongoDbRoute",
          a -> a.weaveAddLast().to("mock:captureGetReadingMongoDb"));
    }
    initialized = true;
    super.resetBeforeAll();
  }

  protected void setupGetSuccessScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(0);
    captureGetReadingMongoDb.expectedMessageCount(1);
  }

  protected void validateGetSuccessScenario() throws Exception {
    captureError.assertIsSatisfied();
    captureGetReadingMongoDb.assertIsSatisfied();
  }
}
