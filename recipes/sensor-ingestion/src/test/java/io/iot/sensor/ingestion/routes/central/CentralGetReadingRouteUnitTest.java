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
@TestProfile(CentralGetReadingRouteUnitTest.class)
@TestInstance(Lifecycle.PER_CLASS)
public class CentralGetReadingRouteUnitTest extends UnitTest {

  @EndpointInject(value = "mock:getReadingMongoDbEndpoint")
  protected MockEndpoint mockGetReadingMongoDbEndpoint;

  private final List<RequestResponseScenario> getReadingScenarios = SensorReadingTestDataProducer.generateGetReadingRequests();

  @Inject
  CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @BeforeAll
  public void setup() throws Exception {
    CentralGetReadingRoute route = new CentralGetReadingRoute(camelBeeRouteConfigurer);
    camelContext.addRoutes(route);

    AdviceWith.adviceWith(camelContext, "centralGetReadingRoute", a -> {
      a.weaveById("getReadingMongoDbEndpoint").replace().to("mock:getReadingMongoDbEndpoint");
    });

    camelContext.start();
  }

  @Test
  @Order(1)
  void given_ValidReadingId_When_GetRouteCalled_Then_BackendCalledWithReading() throws Exception {
    SensorReading reading = getReadingByScenarioName(getReadingScenarios, RequestScenarios.GET_READING_SUCCESS);
    mockGetReadingMongoDbEndpoint.expectedMessageCount(1);
    mockGetReadingMongoDbEndpoint.whenAnyExchangeReceived(
        e -> e.getMessage().setBody(reading));
    var result = callTestRoute(RequestScenarios.GET_READING_SUCCESS);
    mockGetReadingMongoDbEndpoint.assertIsSatisfied();
    assertNull(result.getException());
    assertNotNull(result.getMessage().getBody(SensorReading.class));
  }

  @Test
  @Order(2)
  void given_NotFoundReadingId_When_GetRouteCalled_Then_EmptyBody() throws Exception {
    mockGetReadingMongoDbEndpoint.reset();
    mockGetReadingMongoDbEndpoint.expectedMessageCount(1);
    mockGetReadingMongoDbEndpoint.whenAnyExchangeReceived(
        e -> e.getMessage().setBody(null));
    var result = callTestRoute(RequestScenarios.GET_READING_NOT_FOUND);
    mockGetReadingMongoDbEndpoint.assertIsSatisfied();
    assertNull(result.getMessage().getBody());
  }

  @Test
  @Order(3)
  void given_NullReadingId_When_GetRouteCalled_Then_EmptyBody() throws Exception {
    mockGetReadingMongoDbEndpoint.reset();
    mockGetReadingMongoDbEndpoint.expectedMessageCount(1);
    var result = callTestRoute(RequestScenarios.GET_READING_NULL_ID);
    mockGetReadingMongoDbEndpoint.assertIsSatisfied();
  }

  private Exchange callTestRoute(String scenarioName) throws Exception {
    Map<String, Object> headers = new HashMap<>();
    SensorReading reading = getReadingByScenarioName(getReadingScenarios, scenarioName);
    headers.put("readingId", reading != null ? reading.getReadingId() : null);
    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);
    return fluentProducerTemplate
        .to("direct:centralGetReading")
        .withExchange(exchange)
        .send();
  }
}
