package io.fintech.loan.application.service.routes.central;

import static org.assertj.core.api.Assertions.assertThat;

import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.routes.UnitTest;
import io.fintech.loan.application.service.utils.testdata.ListOrdersDomainTestDataProducer;
import io.fintech.loan.application.service.utils.testdata.ListOrdersDomainTestDataProducer.RequestScenarios;
import io.fintech.loan.application.service.utils.testdata.RequestResponseScenario;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Unit Test for testing sunny and rainy day scenarios for the Ubuy Rest backend
 * in the Order Route.
 *
 * @author camelbee
 *
 */
@SpringBootTest(classes = {
    CentralListOrdersRoute.class,
    CamelBeeRouteConfigurer.class
})
class CentralListOrdersRouteUnitTest extends UnitTest {

  @EndpointInject(value = "mock:listOrdersJpaEndpoint")
  protected MockEndpoint mockListOrdersJpaEndpoint;

  private final List<RequestResponseScenario> listOrdersScenarios = ListOrdersDomainTestDataProducer.generateListOrdersRequests();

  @BeforeEach
  public void setup() throws Exception {

    AdviceWith.adviceWith(camelContext, "centralListOrdersRoute", a -> {
      a.weaveById("listOrdersJpaEndpoint").replace().to("mock:listOrdersJpaEndpoint");
    });

    camelContext.start();
  }

  @Test
  @org.junit.jupiter.api.Order(1)
  void given_InvalidOrder_When_ListOrderRouteCalled_And_ValidationFailed_Then_ResultIsValidationError() throws Exception {

    // Verify mock expectations
    MockEndpoint.expectsMessageCount(0);

    var result = callTestRoute(RequestScenarios.LIST_ORDERS_ERROR_EMPTY_SALES_CHANNEL);

    // Verify result
    assertThat(result.getException()).isInstanceOf(ValidationException.class);

    // Verify mock expectations
    MockEndpoint.assertIsSatisfied(camelContext);

  }

  @Test
  @org.junit.jupiter.api.Order(2)
  void given_ValidOrder_When_ListOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess() throws Exception {

    // Verify mock expectations
    MockEndpoint.expectsMessageCount(1);

    var result = callTestRoute(RequestScenarios.LIST_ORDERS_SUCCESS_PAGE_1);

    // Verify mock expectations
    MockEndpoint.assertIsSatisfied(camelContext);

  }

  private Exchange callTestRoute(String scenarioName) throws Exception {
    Map<String, Object> headers = new HashMap<>();

    // Create a valid Order object for testing
    Order testOrder = getOrderByScenarioName(listOrdersScenarios, scenarioName);

    // Create an exchange with the necessary data
    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);
    exchange.getIn().setBody(testOrder);

    return fluentProducerTemplate
        .to("direct:centralListOrders")
        .withExchange(exchange)
        .send();
  }

}
