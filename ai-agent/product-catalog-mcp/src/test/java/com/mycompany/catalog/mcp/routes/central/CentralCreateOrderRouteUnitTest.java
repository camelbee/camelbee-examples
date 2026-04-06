package com.mycompany.catalog.mcp.routes.central;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.catalog.mcp.model.domain.Order;
import com.mycompany.catalog.mcp.routes.UnitTest;
import com.mycompany.catalog.mcp.utils.testdata.CreateOrderDomainTestDataProducer;
import com.mycompany.catalog.mcp.utils.testdata.CreateOrderDomainTestDataProducer.RequestScenarios;
import com.mycompany.catalog.mcp.utils.testdata.RequestResponseScenario;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

/**
 * Unit Test for testing sunny and rainy day scenarios for the Ubuy Rest backend
 * in the Order Route.
 * 
 * @author camelbee
 *
 */
@QuarkusTest
@TestProfile(CentralCreateOrderRouteUnitTest.class)
@TestInstance(Lifecycle.PER_CLASS)
public class CentralCreateOrderRouteUnitTest extends UnitTest {






  @EndpointInject(value = "mock:createOrderJpaEndpoint")
  protected MockEndpoint mockCreateOrderJpaEndpoint;























  private final List<RequestResponseScenario> createOrderScenarios = CreateOrderDomainTestDataProducer.generateCreateOrderRequests();



    @Inject
    CamelBeeRouteConfigurer camelBeeRouteConfigurer;

    @BeforeAll
    public void setup() throws Exception {
      CentralCreateOrderRoute orderRoute = new CentralCreateOrderRoute(camelBeeRouteConfigurer);
      camelContext.addRoutes(orderRoute);



      AdviceWith.adviceWith(camelContext, "centralCreateOrderRoute", a -> {
          a.weaveById("createOrderJpaEndpoint").replace().to("mock:createOrderJpaEndpoint");
      });

      camelContext.start();
    }


  @Test
  @org.junit.jupiter.api.Order(1)
  void given_InvalidOrder_When_CreateOrderRouteCalled_And_ValidationFailed_Then_ResultIsValidationError() throws Exception {

    // Verify mock expectations
    MockEndpoint.expectsMessageCount(0);

    var result = callTestRoute(RequestScenarios.CREATE_ORDER_ERROR_NO_ITEMS);

    // Verify result
    assertThat(result.getException()).isInstanceOf(ValidationException.class);

    // Verify mock expectations
    MockEndpoint.assertIsSatisfied(camelContext);

  }

  @Test
  @org.junit.jupiter.api.Order(2)
  void given_ValidOrder_When_CreateOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess() throws Exception {

    // Verify mock expectations
    MockEndpoint.expectsMessageCount(1);

    var result = callTestRoute(RequestScenarios.CREATE_ORDER_SUCCESS);

    // Verify mock expectations
    MockEndpoint.assertIsSatisfied(camelContext);

  }

  private Exchange callTestRoute(String scenarioName) throws Exception {
    Map<String, Object> headers = new HashMap<>();

    // Create a valid Order object for testing
    Order testOrder = getOrderByScenarioName(createOrderScenarios, scenarioName);

    // Create an exchange with the necessary data
    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);
    exchange.getIn().setBody(testOrder);

    return fluentProducerTemplate
        .to("direct:centralCreateOrder")
        .withExchange(exchange)
        .send();
  }
}
