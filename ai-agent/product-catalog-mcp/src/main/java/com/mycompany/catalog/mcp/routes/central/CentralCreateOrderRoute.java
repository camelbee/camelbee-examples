package com.mycompany.catalog.mcp.routes.central;

import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.ValidationException;
import com.mycompany.catalog.mcp.model.domain.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.catalog.mcp.constants.Constants;
import com.mycompany.catalog.mcp.exception.GenericExceptionHandler;
import org.camelbee.config.CamelBeeRouteConfigurer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
            throw new ValidationException(exchange,"Order items cannot be empty!");
          }
        })
        .setProperty(Constants.ORIGINAL_BODY, body())
         .to("direct:createOrderJpa").id("createOrderJpaEndpoint")
        .setBody(exchangeProperty(Constants.ACTUAL_RESPONSE_BODY));

  }
}
