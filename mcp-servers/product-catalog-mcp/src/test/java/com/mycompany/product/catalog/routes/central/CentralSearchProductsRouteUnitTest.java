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
@TestProfile(CentralSearchProductsRouteUnitTest.class)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("CentralSearchProductsRoute Unit Tests")
public class CentralSearchProductsRouteUnitTest extends UnitTest {

  @EndpointInject(value = "mock:searchProductsRestEndpoint")
  protected MockEndpoint mockSearchProductsRestEndpoint;

  @EndpointInject(value = "mock:searchWriteAuditLogJpaEndpoint")
  protected MockEndpoint mockWriteAuditLogJpaEndpoint;

  @Inject
  CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @BeforeAll
  public void setup() throws Exception {
    CentralSearchProductsRoute route = new CentralSearchProductsRoute(camelBeeRouteConfigurer);
    camelContext.addRoutes(route);

    AdviceWith.adviceWith(camelContext, "centralSearchProductsRoute", a -> {
      a.weaveById("searchProductsRestEndpoint").replace().to("mock:searchProductsRestEndpoint");
      a.weaveById("searchWriteAuditLogJpaEndpoint").replace().to("mock:searchWriteAuditLogJpaEndpoint");
    });

    camelContext.start();
  }

  @Test
  @org.junit.jupiter.api.Order(1)
  @DisplayName("Should route SearchProducts to REST and JPA backends on success")
  void given_ValidParams_When_SearchProductsRouteCalled_Then_ResultIsSuccess() throws Exception {

    MockEndpoint.expectsMessageCount(1);

    Map<String, Object> headers = new HashMap<>();
    headers.put("page", "1");
    headers.put("pageSize", "10");
    headers.put("query", "wireless");
    headers.put("category", "Electronics");
    headers.put("minPrice", "10");
    headers.put("maxPrice", "100");
    headers.put("inStock", "true");
    headers.put("userId", "test-user");
    headers.put("transactionId", "test-tx");

    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);

    var result = fluentProducerTemplate
        .to("direct:centralSearchProducts")
        .withExchange(exchange)
        .send();

    MockEndpoint.assertIsSatisfied(camelContext);
  }

  @Test
  @org.junit.jupiter.api.Order(2)
  @DisplayName("Should fail validation when page is invalid")
  void given_InvalidPage_When_SearchProductsRouteCalled_Then_ValidationFails() throws Exception {

    MockEndpoint.expectsMessageCount(0);

    Map<String, Object> headers = new HashMap<>();
    headers.put("page", "invalid");
    headers.put("pageSize", "10");
    headers.put("userId", "test-user");

    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);

    var result = fluentProducerTemplate
        .to("direct:centralSearchProducts")
        .withExchange(exchange)
        .send();

    assertThat(result.getException()).isInstanceOf(ValidationException.class);

    MockEndpoint.assertIsSatisfied(camelContext);
  }
}
