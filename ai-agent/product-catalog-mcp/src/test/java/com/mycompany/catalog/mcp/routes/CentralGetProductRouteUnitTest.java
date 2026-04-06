package com.mycompany.catalog.mcp.routes;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.catalog.mcp.routes.central.CentralGetProductRoute;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestProfile(CentralGetProductRouteUnitTest.class)
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CentralGetProductRoute Unit Tests")
public class CentralGetProductRouteUnitTest extends UnitTest {

  @BeforeAll
  public void setup() throws Exception {

    var route = new CentralGetProductRoute(new CamelBeeRouteConfigurer());
    camelContext.addRoutes(route);

    AdviceWith.adviceWith(camelContext, "centralGetProductRoute", a -> {
      a.weaveById("getProductRestEndpoint").replace().to("mock:getProductRestEndpoint");
      a.weaveById("getProductAuditLogEndpoint").replace().to("mock:getProductAuditLogEndpoint");
    });

    camelContext.start();
  }

  @Test
  @Order(1)
  @DisplayName("Should route to REST backend and audit log when valid product ID provided")
  void test_GetProduct_Success() throws Exception {

    MockEndpoint mockRest = camelContext.getEndpoint("mock:getProductRestEndpoint", MockEndpoint.class);
    MockEndpoint mockAudit = camelContext.getEndpoint("mock:getProductAuditLogEndpoint", MockEndpoint.class);
    mockRest.expectedMessageCount(1);
    mockAudit.expectedMessageCount(1);

    Exchange exchange = ExchangeBuilder.anExchange(camelContext)
        .withHeader("productId", "prod-001")
        .build();

    fluentProducerTemplate.to("direct:centralGetProduct")
        .withExchange(exchange).send();

    mockRest.assertIsSatisfied();
    mockAudit.assertIsSatisfied();
  }

  @Test
  @Order(2)
  @DisplayName("Should throw validation error when product ID is empty")
  void test_GetProduct_ValidationError_EmptyId() throws Exception {

    MockEndpoint mockRest = camelContext.getEndpoint("mock:getProductRestEndpoint", MockEndpoint.class);
    mockRest.reset();
    mockRest.expectedMessageCount(0);

    Exchange exchange = ExchangeBuilder.anExchange(camelContext)
        .withHeader("productId", "")
        .build();

    Exchange result = fluentProducerTemplate.to("direct:centralGetProduct")
        .withExchange(exchange).send();

    assertThat(result.getException()).isNotNull();
    mockRest.assertIsSatisfied();
  }

}
