package io.fintech.loan.application.service.routes.central;

import static org.assertj.core.api.Assertions.assertThat;

import io.fintech.loan.application.service.model.domain.Order;
import io.fintech.loan.application.service.routes.UnitTest;
import io.fintech.loan.application.service.utils.testdata.RequestResponseScenario;
import io.fintech.loan.application.service.utils.testdata.UpdateOrderDomainTestDataProducer;
import io.fintech.loan.application.service.utils.testdata.UpdateOrderDomainTestDataProducer.RequestScenarios;
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
    CentralUpdateOrderRoute.class,
    CamelBeeRouteConfigurer.class
})
class CentralUpdateOrderRouteUnitTest extends UnitTest {

  @EndpointInject(value = "mock:updateOrderRestEndpoint")
  protected MockEndpoint mockUpdateOrderRestEndpoint;

  @EndpointInject(value = "mock:updateOrderJpaEndpoint")
  protected MockEndpoint mockUpdateOrderJpaEndpoint;

  @EndpointInject(value = "mock:updateOrderKafkaEndpoint")
  protected MockEndpoint mockUpdateOrderKafkaEndpoint;

  @EndpointInject(value = "mock:updateOrderCacheEndpoint")
  protected MockEndpoint mockUpdateOrderCacheEndpoint;

  private final List<RequestResponseScenario> updateOrderScenarios = UpdateOrderDomainTestDataProducer.generateUpdateOrderRequests();

  @BeforeEach
  public void setup() throws Exception {

    AdviceWith.adviceWith(camelContext, "centralUpdateOrderRoute", a -> {
      a.weaveById("updateOrderRestEndpoint").replace().to("mock:updateOrderRestEndpoint");
      a.weaveById("updateOrderJpaEndpoint").replace().to("mock:updateOrderJpaEndpoint");
      a.weaveById("updateOrderKafkaEndpoint").replace().to("mock:updateOrderKafkaEndpoint");
      a.weaveById("updateOrderCacheEndpoint").replace().to("mock:updateOrderCacheEndpoint");
    });

    camelContext.start();
  }

  @Test
  @org.junit.jupiter.api.Order(1)
  void given_InvalidOrder_When_UpdateOrderRouteCalled_And_ValidationFailed_Then_ResultIsValidationError() throws Exception {

    // Verify mock expectations
    MockEndpoint.expectsMessageCount(0);

    var result = callTestRoute(RequestScenarios.UPDATE_ORDER_ERROR_NO_ITEMS);

    // Verify result
    assertThat(result.getException()).isInstanceOf(ValidationException.class);

    // Verify mock expectations
    MockEndpoint.assertIsSatisfied(camelContext);

  }

  @Test
  @org.junit.jupiter.api.Order(2)
  void given_ValidOrder_When_UpdateOrderRouteCalled_And_AllBackendsSuccessful_Then_ResultIsSuccess() throws Exception {

    // Verify mock expectations
    MockEndpoint.expectsMessageCount(1);

    var result = callTestRoute(RequestScenarios.UPDATE_ORDER_SUCCESS_ID_FORMAT.formatted(1));

    // Verify mock expectations
    MockEndpoint.assertIsSatisfied(camelContext);

  }

  private Exchange callTestRoute(String scenarioName) throws Exception {
    Map<String, Object> headers = new HashMap<>();

    // Create a valid Order object for testing
    Order testOrder = getOrderByScenarioName(updateOrderScenarios, scenarioName);

    // Create an exchange with the necessary data
    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);
    exchange.getIn().setBody(testOrder);

    return fluentProducerTemplate
        .to("direct:centralUpdateOrder")
        .withExchange(exchange)
        .send();
  }

}
