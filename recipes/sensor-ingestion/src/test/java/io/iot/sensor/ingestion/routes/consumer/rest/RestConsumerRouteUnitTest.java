package io.iot.sensor.ingestion.routes.consumer.rest;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iot.sensor.ingestion.constants.Constants;
import io.iot.sensor.ingestion.exception.GenericExceptionHandler;
import io.iot.sensor.ingestion.mapper.api.JsonSensorReadingMapper;
import io.iot.sensor.ingestion.routes.UnitTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@QuarkusTest
@TestProfile(RestConsumerRouteUnitTest.class)
@TestInstance(Lifecycle.PER_CLASS)
public class RestConsumerRouteUnitTest extends UnitTest {

  @Inject
  CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Inject
  GenericExceptionHandler genericExceptionHandler;

  @Inject
  ObjectMapper objectMapper;

  @Inject
  JsonSensorReadingMapper jsonSensorReadingMapper;

  @EndpointInject(value = "mock:centralListReadings")
  protected MockEndpoint mockCentralListReadings;

  @EndpointInject(value = "mock:centralGetReading")
  protected MockEndpoint mockCentralGetReading;

  private Exchange exchange;

  @BeforeAll
  public void setup() throws Exception {
    RestConsumerRoute route = new RestConsumerRoute(camelBeeRouteConfigurer,
        genericExceptionHandler, objectMapper, jsonSensorReadingMapper);
    camelContext.addRoutes(route);

    AdviceWith.adviceWith(camelContext, "listSensorReadingsRoute", a -> a.weaveByToUri("direct:centralListReadings").replace().to("mock:centralListReadings"));
    AdviceWith.adviceWith(camelContext, "getSensorReadingOperationRoute", a -> a.weaveByToUri("direct:centralGetReading").replace().to(
        "mock:centralGetReading"));

    camelContext.start();
  }

  @BeforeEach
  public void init() {
    exchange = ExchangeBuilder.anExchange(camelContext).build();
  }

  @Test
  @Order(1)
  void given_NoContentTypeHeader_When_ListSensorReadings_Then_UseDefaultContentType() throws Exception {
    mockCentralListReadings.expectedMessageCount(1);
    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
    exchange.getIn().setBody(null);
    fluentProducerTemplate.to("direct:listSensorReadings").withExchange(exchange).send();
    mockCentralListReadings.assertIsSatisfied(camelContext);
    assertEquals("application/json", exchange.getProperty(Constants.ORIGINAL_CONTENT_TYPE));
  }

  @Test
  @Order(2)
  void given_JsonContentType_When_ListSensorReadings_Then_PreserveContentType() throws Exception {
    mockCentralListReadings.expectedMessageCount(1);
    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/xml");
    exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
    exchange.getIn().setBody(null);
    fluentProducerTemplate.to("direct:listSensorReadings").withExchange(exchange).send();
    mockCentralListReadings.assertIsSatisfied(camelContext);
    assertEquals("application/xml", exchange.getProperty(Constants.ORIGINAL_CONTENT_TYPE));
  }

  @Test
  @Order(3)
  void given_NoAcceptHeader_When_ListSensorReadings_Then_UseContentTypeAsAccept() throws Exception {
    mockCentralListReadings.expectedMessageCount(1);
    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
    exchange.getIn().setBody(null);
    fluentProducerTemplate.to("direct:listSensorReadings").withExchange(exchange).send();
    mockCentralListReadings.assertIsSatisfied(camelContext);
    assertEquals("application/json", exchange.getProperty(Constants.ORIGINAL_ACCEPT_CONTENT_TYPE));
  }

  @Test
  @Order(4)
  void given_EmptyAcceptHeader_When_ListSensorReadings_Then_UseContentTypeAsAccept() throws Exception {
    mockCentralListReadings.expectedMessageCount(1);
    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
    exchange.getIn().setHeader("Accept", "");
    exchange.getIn().setBody(null);
    fluentProducerTemplate.to("direct:listSensorReadings").withExchange(exchange).send();
    mockCentralListReadings.assertIsSatisfied(camelContext);
    assertEquals("application/json", exchange.getProperty(Constants.ORIGINAL_ACCEPT_CONTENT_TYPE));
  }

  @Test
  @Order(5)
  void given_SpecificAcceptHeader_When_ListSensorReadings_Then_PreserveAcceptHeader() throws Exception {
    mockCentralListReadings.expectedMessageCount(1);
    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
    exchange.getIn().setHeader("Accept", "application/xml");
    exchange.getIn().setBody(null);
    fluentProducerTemplate.to("direct:listSensorReadings").withExchange(exchange).send();
    mockCentralListReadings.assertIsSatisfied(camelContext);
    assertEquals("application/xml", exchange.getProperty(Constants.ORIGINAL_ACCEPT_CONTENT_TYPE));
  }

  @Test
  @Order(6)
  void given_ValidReadingId_When_GetSensorReading_Then_ForwardToCentral() throws Exception {
    mockCentralGetReading.expectedMessageCount(1);
    exchange.getIn().setHeader("readingId", "test-id-001");
    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
    exchange.getIn().setBody(null);
    fluentProducerTemplate.to("direct:getSensorReading").withExchange(exchange).send();
    mockCentralGetReading.assertIsSatisfied(camelContext);
  }
}
