package io.iot.sensor.ingestion.itest;

import java.util.Arrays;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;

public class IngestReadingIntegrationTest extends IntegrationTest {

  protected static final String INGEST_BASE_PATH_API = BASE_PATH_API + "%s/ingest/";

  @EndpointInject("mock:captureIngestReadingMongoDb")
  protected MockEndpoint captureIngestReadingMongoDb;

  @EndpointInject("mock:captureIngestReadingKafka")
  protected MockEndpoint captureIngestReadingKafka;

  private static boolean initialized = false;

  public void setup() throws Exception {
    captureMockEndpoints = Arrays.asList(captureError, captureIngestReadingMongoDb, captureIngestReadingKafka);
    verifyMockEndpoints = Arrays.asList();
    super.setup();

    if (!initialized) {
      var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);

      var routeKafkaConsumer = new RouteDefinition();
      routeKafkaConsumer.from("kafka:{{camelbeeservice.southbound-ingest-topic}}"
          + "?groupId=camelbeeinttest&autoOffsetReset=earliest")
          .to("mock:captureIngestReadingKafka");
      modelContext.addRouteDefinition(routeKafkaConsumer);

      AdviceWith.adviceWith(camelContext, "ingestReadingKafkaRoute",
          a -> a.weaveAddLast().to("mock:captureIngestReadingKafka"));
      AdviceWith.adviceWith(camelContext, "ingestReadingMongoDbRoute",
          a -> a.weaveAddLast().to("mock:captureIngestReadingMongoDb"));
    }
    initialized = true;
    super.resetBeforeAll();
  }

  protected void setupIngestSuccessScenario() throws Exception {
    resetAllMockedEndpoints();
    captureError.expectedMessageCount(0);
    captureIngestReadingMongoDb.expectedMessageCount(1);
    captureIngestReadingKafka.expectedMessageCount(1);
    clearMongoDbTables();
  }

  protected void validateIngestSuccessScenario() throws Exception {
    captureError.assertIsSatisfied();
    captureIngestReadingKafka.assertIsSatisfied();
    captureIngestReadingMongoDb.assertIsSatisfied();
  }

}
