package io.fintech.loan.application.service.routes.central;

import io.fintech.loan.application.service.constants.Constants;
import io.fintech.loan.application.service.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        .to("direct:listOrdersJpa").id("listOrdersJpaEndpoint");
  }
}
