package com.mycompany.catalog.mcp.routes.central;

import com.mycompany.catalog.mcp.constants.Constants;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * Central route for getting a single product by ID.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CentralGetProductRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralGetProduct").routeId("centralGetProductRoute")
        .setProperty(Constants.ORIGINAL_BODY, body())
        .process(exchange -> {
          String productId = exchange.getIn().getHeader("productId", String.class);
          if (productId == null || productId.isBlank()) {
            throw new ValidationException(exchange, "Product ID cannot be empty!");
          }
        })
        .to("direct:getProductRest").id("getProductRestEndpoint")
        .setBody(exchangeProperty(Constants.ACTUAL_RESPONSE_BODY))
        .to("direct:writeAuditLogJpa").id("getProductAuditLogEndpoint");
  }
}
