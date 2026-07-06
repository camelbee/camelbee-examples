package io.iot.sensor.ingestion.routes.central;

import static org.junit.jupiter.api.Assertions.*;

import io.iot.sensor.ingestion.model.domain.SensorReadingPage;
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
@TestProfile(CentralListReadingsRouteUnitTest.class)
@TestInstance(Lifecycle.PER_CLASS)
public class CentralListReadingsRouteUnitTest extends UnitTest {

  @EndpointInject(value = "mock:listReadingsMongoDbEndpoint")
  protected MockEndpoint mockListReadingsMongoDbEndpoint;

  private final List<RequestResponseScenario> listReadingsScenarios = SensorReadingTestDataProducer.generateListReadingsRequests();

  @Inject
  CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @BeforeAll
  public void setup() throws Exception {
    CentralListReadingsRoute route = new CentralListReadingsRoute(camelBeeRouteConfigurer);
    camelContext.addRoutes(route);

    AdviceWith.adviceWith(camelContext, "centralListReadingsRoute", a -> {
      a.weaveById("listReadingsMongoDbEndpoint").replace().to("mock:listReadingsMongoDbEndpoint");
    });

    camelContext.start();
  }

  @Test
  @Order(1)
  void given_ValidDeviceId_When_ListRouteCalled_Then_BackendCalledWithPage() throws Exception {
    SensorReadingPage page = SensorReadingPage.builder()
        .readings(List.of())
        .totalItems(0)
        .page(0)
        .pageSize(50)
        .deviceId("device-001")
        .build();
    mockListReadingsMongoDbEndpoint.expectedMessageCount(1);
    mockListReadingsMongoDbEndpoint.whenAnyExchangeReceived(
        e -> e.getMessage().setBody(page));
    var result = callTestRoute(RequestScenarios.LIST_READINGS_SUCCESS);
    mockListReadingsMongoDbEndpoint.assertIsSatisfied();
    assertNull(result.getException());
    assertNotNull(result.getMessage().getBody());
  }

  @Test
  @Order(2)
  void given_InvalidDeviceId_When_ListRouteCalled_Then_Exception() throws Exception {
    mockListReadingsMongoDbEndpoint.reset();
    mockListReadingsMongoDbEndpoint.expectedMessageCount(0);
    var result = callTestRoute(RequestScenarios.LIST_READINGS_INVALID_DEVICE_ID);
    mockListReadingsMongoDbEndpoint.assertIsSatisfied();
    assertNotNull(result.getException());
  }

  private Exchange callTestRoute(String scenarioName) throws Exception {
    Map<String, Object> headers = new HashMap<>();
    setHeaderFromScenario(headers, listReadingsScenarios, scenarioName, "deviceId");
    setHeaderFromScenario(headers, listReadingsScenarios, scenarioName, "page");
    setHeaderFromScenario(headers, listReadingsScenarios, scenarioName, "pageSize");
    setHeaderFromScenario(headers, listReadingsScenarios, scenarioName, "from");
    setHeaderFromScenario(headers, listReadingsScenarios, scenarioName, "to");
    setHeaderFromScenario(headers, listReadingsScenarios, scenarioName, "sensorType");
    setHeaderFromScenario(headers, listReadingsScenarios, scenarioName, "minQuality");
    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);
    return fluentProducerTemplate
        .to("direct:centralListReadings")
        .withExchange(exchange)
        .send();
  }
}
