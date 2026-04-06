package com.mycompany.catalog.mcp.routes;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.catalog.mcp.routes.central.CentralListProductsRoute;
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
@TestProfile(CentralListProductsRouteUnitTest.class)
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CentralListProductsRoute Unit Tests")
public class CentralListProductsRouteUnitTest extends UnitTest {

  @BeforeAll
  public void setup() throws Exception {

    var route = new CentralListProductsRoute(new CamelBeeRouteConfigurer());
    camelContext.addRoutes(route);

    AdviceWith.adviceWith(camelContext, "centralListProductsRoute", a -> {
      a.weaveById("listProductsRestEndpoint").replace().to("mock:listProductsRestEndpoint");
      a.weaveById("listProductsAuditLogEndpoint").replace().to("mock:listProductsAuditLogEndpoint");
    });

    camelContext.start();
  }

  @Test
  @Order(1)
  @DisplayName("Should route to REST backend and audit log when valid parameters provided")
  void test_ListProducts_Success() throws Exception {

    MockEndpoint mockRest = camelContext.getEndpoint("mock:listProductsRestEndpoint", MockEndpoint.class);
    MockEndpoint mockAudit = camelContext.getEndpoint("mock:listProductsAuditLogEndpoint", MockEndpoint.class);
    mockRest.expectedMessageCount(1);
    mockAudit.expectedMessageCount(1);

    Exchange exchange = ExchangeBuilder.anExchange(camelContext)
        .withHeader("page", "1")
        .withHeader("pageSize", "10")
        .build();

    fluentProducerTemplate.to("direct:centralListProducts")
        .withExchange(exchange).send();

    mockRest.assertIsSatisfied();
    mockAudit.assertIsSatisfied();
  }

  @Test
  @Order(2)
  @DisplayName("Should throw validation error when page is invalid")
  void test_ListProducts_ValidationError_InvalidPage() throws Exception {

    MockEndpoint mockRest = camelContext.getEndpoint("mock:listProductsRestEndpoint", MockEndpoint.class);
    mockRest.reset();
    mockRest.expectedMessageCount(0);

    Exchange exchange = ExchangeBuilder.anExchange(camelContext)
        .withHeader("page", "0")
        .withHeader("pageSize", "10")
        .build();

    Exchange result = fluentProducerTemplate.to("direct:centralListProducts")
        .withExchange(exchange).send();

    assertThat(result.getException()).isNotNull();
    mockRest.assertIsSatisfied();
  }

}
