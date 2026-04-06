package com.mycompany.catalog.mcp.routes.central;

import com.mycompany.catalog.mcp.constants.Constants;
import com.mycompany.catalog.mcp.utils.ValidationUtils;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;

/**
 * Central route for searching products with filters.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CentralSearchProductsRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralSearchProducts").routeId("centralSearchProductsRoute")
        .setProperty(Constants.ORIGINAL_BODY, body())
        .process(exchange -> {
          ValidationUtils.validateNumericHeader(exchange, "page", 1, Integer.MAX_VALUE);
          ValidationUtils.validateNumericHeader(exchange, "pageSize", 1, 100);
        })
        .to("direct:searchProductsRest").id("searchProductsRestEndpoint")
        .setBody(exchangeProperty(Constants.ACTUAL_RESPONSE_BODY))
        .to("direct:writeAuditLogJpa").id("searchProductsAuditLogEndpoint");
  }
}
