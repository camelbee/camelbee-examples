package io.iot.sensor.ingestion.routes.central;

import static org.junit.jupiter.api.Assertions.*;

import io.iot.sensor.ingestion.model.domain.SensorReading;
import io.iot.sensor.ingestion.routes.UnitTest;
import io.iot.sensor.ingestion.utils.testdata.RequestResponseScenario;
import io.iot.sensor.ingestion.utils.testdata.SensorReadingTestDataProducer;
import io.iot.sensor.ingestion.utils.testdata.SensorReadingTestDataProducer.RequestScenarios;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@QuarkusTest
@TestProfile(CentralIngestReadingRouteUnitTest.class)
@TestInstance(Lifecycle.PER_CLASS)
public class CentralIngestReadingRouteUnitTest extends UnitTest {

  @EndpointInject(value = "mock:ingestReadingMongoDbEndpoint")
  protected MockEndpoint mockIngestReadingMongoDbEndpoint;

  @EndpointInject(value = "mock:ingestReadingKafkaEndpoint")
  protected MockEndpoint mockIngestReadingKafkaEndpoint;

  private final List<RequestResponseScenario> ingestScenarios = SensorReadingTestDataProducer.generateIngestReadingRequests();

  @Inject
  CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @BeforeAll
  public void setup() throws Exception {
    CentralIngestReadingRoute route = new CentralIngestReadingRoute(camelBeeRouteConfigurer);
    camelContext.addRoutes(route);

    AdviceWith.adviceWith(camelContext, "centralIngestReadingRoute", a -> {
      a.weaveById("ingestReadingMongoDbEndpoint").replace().to("mock:ingestReadingMongoDbEndpoint");
      a.weaveById("ingestReadingKafkaEndpoint").replace().to("mock:ingestReadingKafkaEndpoint");
    });

    camelContext.start();
  }

  @Test
  @Order(1)
  void given_ValidReading_When_IngestRouteCalled_Then_AllBackendsCalled() throws Exception {
    mockIngestReadingMongoDbEndpoint.expectedMessageCount(1);
    mockIngestReadingKafkaEndpoint.expectedMessageCount(1);
    var result = callTestRoute(RequestScenarios.INGEST_READING_SUCCESS);
    mockIngestReadingMongoDbEndpoint.assertIsSatisfied();
    mockIngestReadingKafkaEndpoint.assertIsSatisfied();
    assertNull(result.getException());
  }

  @Test
  @Order(2)
  void given_InvalidReading_When_IngestRouteCalled_Then_BackendsStillCalled() throws Exception {
    mockIngestReadingMongoDbEndpoint.reset();
    mockIngestReadingKafkaEndpoint.reset();
    mockIngestReadingMongoDbEndpoint.expectedMessageCount(1);
    mockIngestReadingKafkaEndpoint.expectedMessageCount(1);
    var result = callTestRoute(RequestScenarios.INGEST_READING_INVALID);
    mockIngestReadingMongoDbEndpoint.assertIsSatisfied();
    mockIngestReadingKafkaEndpoint.assertIsSatisfied();
  }

  private Exchange callTestRoute(String scenarioName) throws Exception {
    Map<String, Object> headers = new HashMap<>();
    SensorReading reading = getReadingByScenarioName(ingestScenarios, scenarioName);
    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);
    exchange.getIn().setBody(reading);
    return fluentProducerTemplate
        .to("direct:centralIngestReading")
        .withExchange(exchange)
        .send();
  }
}
