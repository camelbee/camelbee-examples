package com.mycompany.catalog.mcp.routes.central;

import org.apache.camel.builder.RouteBuilder;

import com.mycompany.catalog.mcp.utils.ValidationUtils;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.component.jackson.JacksonDataFormat;

import java.util.Arrays;
import java.util.Optional;
import lombok.SneakyThrows;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
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
public class CentralListOrdersRoute extends RouteBuilder {

  final CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @Override
  public void configure() throws Exception {

    camelBeeRouteConfigurer.configureRoute(this);
    errorHandler(noErrorHandler());

    from("direct:centralListOrders").routeId("centralListOrdersRoute")
      .setProperty(Constants.ORIGINAL_BODY, body())
        .process(exchange -> {
          // Validate header values to prevent injection vulnerabilities
          ValidationUtils.validateNumericHeader(exchange, "page", 1, Integer.MAX_VALUE);
          ValidationUtils.validateNumericHeader(exchange, "pageSize", 1, 100);
          ValidationUtils.validateSalesChannel(exchange);
        })
         .to("direct:listOrdersRest").id("listOrdersRestEndpoint")
    ;
  }
}
