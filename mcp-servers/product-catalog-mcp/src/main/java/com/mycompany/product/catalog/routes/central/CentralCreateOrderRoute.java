package com.mycompany.product.catalog.routes.central;

import com.mycompany.product.catalog.constants.Constants;
import com.mycompany.product.catalog.model.domain.Order;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * Order Route.
 *
 * @author camelbee
 */
@ApplicationScoped
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
        .setBody(exchangeProperty(Constants.ACTUAL_RESPONSE_BODY));

  }
}
