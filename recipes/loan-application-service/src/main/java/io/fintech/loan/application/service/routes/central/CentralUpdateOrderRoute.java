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
 * Update Order Route.
 *
 * @author camelbee
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CentralUpdateOrderRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralUpdateOrder").routeId("centralUpdateOrderRoute")
        .process(exchange -> {
          Order order = exchange.getIn().getBody(Order.class);
          if (order.getId() == null) {
            throw new ValidationException(exchange, "Order id cannot be empty!");
          }
          if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new ValidationException(exchange, "Order items cannot be empty!");
          }
        })
        .setProperty(Constants.ORIGINAL_BODY, body())
        .to("direct:updateOrderRest").id("updateOrderRestEndpoint")
        .to("direct:updateOrderJpa").id("updateOrderJpaEndpoint")
        .to("direct:updateOrderKafka").id("updateOrderKafkaEndpoint")
        .to("direct:updateOrderCache").id("updateOrderCacheEndpoint")
        .setBody(exchangeProperty(Constants.ACTUAL_RESPONSE_BODY));

  }
}
