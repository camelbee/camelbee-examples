package com.mycompany.product.catalog.routes.central;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.product.catalog.routes.UnitTest;
import com.mycompany.product.catalog.utils.testdata.ListProductsDomainTestDataProducer;
import com.mycompany.product.catalog.utils.testdata.ListProductsDomainTestDataProducer.RequestScenarios;
import com.mycompany.product.catalog.utils.testdata.RequestResponseScenario;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@QuarkusTest
@TestProfile(CentralListProductsRouteUnitTest.class)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("CentralListProductsRoute Unit Tests")
public class CentralListProductsRouteUnitTest extends UnitTest {

  @EndpointInject(value = "mock:listProductsRestEndpoint")
  protected MockEndpoint mockListProductsRestEndpoint;

  @EndpointInject(value = "mock:writeAuditLogJpaEndpoint")
  protected MockEndpoint mockWriteAuditLogJpaEndpoint;

  private final List<RequestResponseScenario> scenarios = ListProductsDomainTestDataProducer.generateListProductsRequests();

  @Inject
  CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @BeforeAll
  public void setup() throws Exception {
    CentralListProductsRoute route = new CentralListProductsRoute(camelBeeRouteConfigurer);
    camelContext.addRoutes(route);

    AdviceWith.adviceWith(camelContext, "centralListProductsRoute", a -> {
      a.weaveById("listProductsRestEndpoint").replace().to("mock:listProductsRestEndpoint");
      a.weaveById("writeAuditLogJpaEndpoint").replace().to("mock:writeAuditLogJpaEndpoint");
    });

    camelContext.start();
  }

  @Test
  @org.junit.jupiter.api.Order(1)
  @DisplayName("Should route ListProducts to REST and JPA backends on success")
  void given_ValidParams_When_ListProductsRouteCalled_Then_ResultIsSuccess() throws Exception {

    MockEndpoint.expectsMessageCount(1);

    RequestResponseScenario scenario = getScenarioByName(scenarios, RequestScenarios.LIST_PRODUCTS_SUCCESS_PAGE_1);

    Map<String, Object> headers = new HashMap<>();
    headers.put("page", scenario.getPage());
    headers.put("pageSize", scenario.getPageSize());
    headers.put("userId", "test-user");
    headers.put("transactionId", scenario.getTransactionId());

    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);

    var result = fluentProducerTemplate
        .to("direct:centralListProducts")
        .withExchange(exchange)
        .send();

    MockEndpoint.assertIsSatisfied(camelContext);
  }

  @Test
  @org.junit.jupiter.api.Order(2)
  @DisplayName("Should fail validation when page is invalid")
  void given_InvalidPage_When_ListProductsRouteCalled_Then_ValidationFails() throws Exception {

    MockEndpoint.expectsMessageCount(0);

    Map<String, Object> headers = new HashMap<>();
    headers.put("page", "invalid");
    headers.put("pageSize", "5");
    headers.put("userId", "test-user");

    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);

    var result = fluentProducerTemplate
        .to("direct:centralListProducts")
        .withExchange(exchange)
        .send();

    assertThat(result.getException()).isInstanceOf(org.apache.camel.ValidationException.class);

    MockEndpoint.assertIsSatisfied(camelContext);
  }
}
