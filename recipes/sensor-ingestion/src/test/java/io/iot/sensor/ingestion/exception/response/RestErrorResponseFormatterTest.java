package io.iot.sensor.ingestion.exception.response;

import static org.junit.jupiter.api.Assertions.*;

import io.iot.sensor.ingestion.constants.Constants;
import io.iot.sensor.ingestion.exception.ErrorMeta;
import io.iot.sensor.ingestion.model.domain.Error;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RestErrorResponseFormatterTest {

  private RestErrorResponseFormatter formatter;
  private CamelContext context;

  @BeforeEach
  void setUp() {
    formatter = new RestErrorResponseFormatter();
    context = new DefaultCamelContext();
  }

  @Test
  void given_PlatformHttpEndpoint_When_Supports_Then_ReturnTrue() throws Exception {
    context.start();
    try {
      Endpoint endpoint = context.getEndpoint("platform-http:/test");
      Exchange exchange = DefaultExchange.newFromEndpoint(endpoint);
      assertTrue(formatter.supports(exchange));
    } finally {
      context.stop();
    }
  }

  @Test
  void given_NonHttpEndpoint_When_Supports_Then_ReturnFalse() throws Exception {
    context.start();
    try {
      Endpoint endpoint = context.getEndpoint("direct:test");
      Exchange exchange = DefaultExchange.newFromEndpoint(endpoint);
      assertFalse(formatter.supports(exchange));
    } finally {
      context.stop();
    }
  }

  @Test
  void getPriority_ShouldReturn10() {
    assertEquals(10, formatter.getPriority());
  }

  @Test
  void given_ErrorMetaWithAcceptContentType_When_Format_Then_SetResponseHeadersAndBody() {
    Exchange exchange = new DefaultExchange(context);
    exchange.setProperty(Constants.ORIGINAL_ACCEPT_CONTENT_TYPE, "application/json");
    ErrorMeta meta = ErrorMeta.of("ERROR-TEST-001", "Test error", 400);

    formatter.format(exchange, meta);

    assertEquals(400, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE));
    assertEquals("application/json", exchange.getIn().getHeader(Exchange.CONTENT_TYPE));
    Error body = exchange.getMessage().getBody(Error.class);
    assertNotNull(body);
    assertEquals("ERROR-TEST-001", body.getCode());
    assertEquals("Test error", body.getMessage());
  }

  @Test
  void given_ErrorMetaWithoutAcceptContentType_When_Format_Then_FallbackToOriginalContentType() {
    Exchange exchange = new DefaultExchange(context);
    exchange.setProperty(Constants.ORIGINAL_CONTENT_TYPE, "application/xml");
    ErrorMeta meta = ErrorMeta.of("ERROR-TEST-002", "Another error", 404);

    formatter.format(exchange, meta);

    assertEquals(404, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE));
    Error body = exchange.getMessage().getBody(Error.class);
    assertNotNull(body);
    assertEquals("ERROR-TEST-002", body.getCode());
    assertEquals("Another error", body.getMessage());
  }
}
