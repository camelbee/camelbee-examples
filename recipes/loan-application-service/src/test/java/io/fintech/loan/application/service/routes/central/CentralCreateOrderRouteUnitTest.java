package io.fintech.loan.application.service.routes.central;

import static org.assertj.core.api.Assertions.assertThat;

import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.routes.UnitTest;
import io.fintech.loan.application.service.utils.testdata.CreateOrderDomainTestDataProducer;
import io.fintech.loan.application.service.utils.testdata.CreateOrderDomainTestDataProducer.RequestScenarios;
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
    CentralCreateOrderRoute.class,
    CamelBeeRouteConfigurer.class
})
class CentralCreateOrderRouteUnitTest extends UnitTest {

  @EndpointInject(value = "mock:createOrderJpaEndpoint")
  protected MockEndpoint mockCreateOrderJpaEndpoint;

  @EndpointInject(value = "mock:createOrderKafkaEndpoint")
  protected MockEndpoint mockCreateOrderKafkaEndpoint;

  @EndpointInject(value = "mock:createOrderCacheEndpoint")
  protected MockEndpoint mockCreateOrderCacheEndpoint;

  private final List<RequestResponseScenario> createOrderScenarios = CreateOrderDomainTestDataProducer.generateCreateOrderRequests();

  @BeforeEach
  public void setup() throws Exception {

    AdviceWith.adviceWith(camelContext, "centralCreateOrderRoute", a -> {
      a.weaveById("createOrderJpaEndpoint").replace().to("mock:createOrderJpaEndpoint");
      a.weaveById("createOrderKafkaEndpoint").replace().to("mock:createOrderKafkaEndpoint");
      a.weaveById("createOrderCacheEndpoint").replace().to("mock:createOrderCacheEndpoint");
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
