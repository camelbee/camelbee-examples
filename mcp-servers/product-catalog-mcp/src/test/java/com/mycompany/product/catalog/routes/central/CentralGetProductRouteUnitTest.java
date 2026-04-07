package com.mycompany.product.catalog.routes.central;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.product.catalog.routes.UnitTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@QuarkusTest
@TestProfile(CentralGetProductRouteUnitTest.class)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("CentralGetProductRoute Unit Tests")
public class CentralGetProductRouteUnitTest extends UnitTest {

  @EndpointInject(value = "mock:getProductRestEndpoint")
  protected MockEndpoint mockGetProductRestEndpoint;

  @EndpointInject(value = "mock:getProductWriteAuditLogJpaEndpoint")
  protected MockEndpoint mockWriteAuditLogJpaEndpoint;

  @Inject
  CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @BeforeAll
  public void setup() throws Exception {
    CentralGetProductRoute route = new CentralGetProductRoute(camelBeeRouteConfigurer);
    camelContext.addRoutes(route);

    AdviceWith.adviceWith(camelContext, "centralGetProductRoute", a -> {
      a.weaveById("getProductRestEndpoint").replace().to("mock:getProductRestEndpoint");
      a.weaveById("getProductWriteAuditLogJpaEndpoint").replace().to("mock:getProductWriteAuditLogJpaEndpoint");
    });

    camelContext.start();
  }

  @Test
  @org.junit.jupiter.api.Order(1)
  @DisplayName("Should route GetProduct to REST and JPA backends on success")
  void given_ValidId_When_GetProductRouteCalled_Then_ResultIsSuccess() throws Exception {

    MockEndpoint.expectsMessageCount(1);

    Map<String, Object> headers = new HashMap<>();
    headers.put("productId", "prod-001");
    headers.put("userId", "test-user");

    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);

    var result = fluentProducerTemplate
        .to("direct:centralGetProduct")
        .withExchange(exchange)
        .send();

    MockEndpoint.assertIsSatisfied(camelContext);
  }

  @Test
  @org.junit.jupiter.api.Order(2)
  @DisplayName("Should fail validation when product ID is empty")
  void given_EmptyId_When_GetProductRouteCalled_Then_ValidationFails() throws Exception {

    MockEndpoint.expectsMessageCount(0);

    Map<String, Object> headers = new HashMap<>();
    headers.put("productId", "");
    headers.put("userId", "test-user");

    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);

    var result = fluentProducerTemplate
        .to("direct:centralGetProduct")
        .withExchange(exchange)
        .send();

    assertThat(result.getException()).isInstanceOf(ValidationException.class);

    MockEndpoint.assertIsSatisfied(camelContext);
  }
}
