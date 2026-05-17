package io.fintech.loan.application.service.routes.central;

import io.fintech.loan.application.service.constants.Constants;
import io.fintech.loan.application.service.model.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.springframework.stereotype.Component;

/**
 * Order Route.
 *
 * @author camelbee
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CentralCreateOrderRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralCreateOrder").routeId("centralCreateOrderRoute")
        .process(exchange -> {
          Order order = exchange.getIn().getBody(Order.class);
          if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new ValidationException(exchange, "Order items cannot be empty!");
          }
        })
        .setProperty(Constants.ORIGINAL_BODY, body())
        .to("direct:createOrderJpa").id("createOrderJpaEndpoint")
        .to("direct:createOrderKafka").id("createOrderKafkaEndpoint")
        .to("direct:createOrderCache").id("createOrderCacheEndpoint")
        .setBody(exchangeProperty(Constants.ACTUAL_RESPONSE_BODY));

  }
}
